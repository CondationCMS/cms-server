package com.condation.cms.content;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.extensions.ContentQueryOperatorExtensionPoint;
import com.condation.cms.api.extensions.TemplateModelExtendingExtentionPoint;
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.feature.features.ServerPropertiesFeature;
import com.condation.cms.api.feature.features.SiteMediaServiceFeature;
import com.condation.cms.api.messages.MessageSource;
import com.condation.cms.api.model.ListNode;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.content.pipeline.ContentPipelineFactory;
import com.condation.cms.content.views.model.View;
import com.condation.cms.extensions.hooks.DBHooks;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.content.template.functions.LinkFunction;
import com.condation.cms.content.template.functions.list.NodeListFunctionBuilder;
import com.condation.cms.content.template.functions.navigation.NavigationFunction;
import com.condation.cms.content.template.functions.query.QueryFunction;
import com.condation.cms.content.template.functions.shortcode.ShortCodeTemplateFunction;
import com.condation.cms.content.template.functions.taxonomy.TaxonomyFunction;
import com.condation.modules.api.ModuleManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultContentRenderer implements ContentRenderer {

	private final ContentParser contentParser;
	private final Supplier<TemplateEngine> templates;
	private final DB db;
	private final SiteProperties siteProperties;
	private final ModuleManager moduleManager;

	@Override
	public String render(final ReadOnlyFile contentFile, final RequestContext context) throws IOException {
		return render(contentFile, context, Collections.emptyMap());
	}

	@Override
	public String render(final ReadOnlyFile contentFile, final RequestContext context, final Map<String, List<Section>> sections) throws IOException {
		var content = contentParser.parse(contentFile);

		return render(contentFile, context, sections, content.meta(), content.content(), (model) -> {
		});
	}

	@Override
	public String renderTaxonomy(final Taxonomy taxonomy, Optional<String> taxonomyValue, final RequestContext context, final Map<String, Object> meta, final Page<ListNode> page) throws IOException {
		var contentFile = db.getReadOnlyFileSystem().contentBase().resolve("index.md");

		return render(contentFile, context, Collections.emptyMap(), meta, "", (model) -> {
			model.values.put("taxonomy", taxonomy);
			model.values.put("taxonomy_values", db.getTaxonomies().values(taxonomy));
			if (taxonomyValue.isPresent()) {
				model.values.put("taxonomy_value", taxonomyValue.get());
			}
			model.values.put("page", page);

		});
	}

	@Override
	public String renderView(final ReadOnlyFile viewFile, final View view, final ContentNode contentNode, final RequestContext requestContext, final Page<ListNode> page) throws IOException {
		return render(viewFile, requestContext, Collections.emptyMap(),
				contentNode.data(), "", (model) -> {
			model.values.put("page", page);
		});
	}

	private <F extends Feature> Object getFeatureValueOrDefault(RequestContext context,
			Class<F> feature, Function<F, Object> valueFunction, Object defaultValue) {
		if (context.has(feature)) {
			return valueFunction.apply(context.get(feature));
		}
		return defaultValue;
	}

	private String renderMarkdown(final String rawContent, final RequestContext context, final TemplateEngine.Model model) {
		var pipeline = ContentPipelineFactory.create(context, model);
		
		return pipeline.process(rawContent);
	}

	@Override
	public String render(final ReadOnlyFile contentFile, final RequestContext context,
			final Map<String, List<Section>> sections,
			final Map<String, Object> meta, final String rawContent, final Consumer<TemplateEngine.Model> modelExtending
	) throws IOException {
		var uri = PathUtil.toRelativeFile(contentFile, db.getReadOnlyFileSystem().contentBase());
		
		Optional<ContentNode> contentNode = db.getContent().byUri(uri);

		TemplateEngine.Model model = new TemplateEngine.Model(contentFile, contentNode.isPresent() ? contentNode.get() : null);

		modelExtending.accept(model);

		model.values.put("meta", meta);
		model.values.put("sections", sections);

		model.values.put("shortCodes", createShortCodeFunction(context));
		model.values.put("navigation", createNavigationFunction(contentFile, context));
		model.values.put("nodeList", createNodeListFunction(contentFile, context));
		model.values.put("query", createQueryFunction(contentFile, context));
		model.values.put("requestContext", context.get(RequestFeature.class));
		model.values.put("theme", context.get(RenderContext.class).theme());
		model.values.put("site", siteProperties);
		model.values.put("mediaService", context.get(SiteMediaServiceFeature.class).mediaService());

		model.values.put("taxonomies", context.get(InjectorFeature.class).injector().getInstance(TaxonomyFunction.class));
		
		var theme = context.get(RenderContext.class).theme();
		if (theme.empty()) {
			model.values.put("messages", context.get(InjectorFeature.class).injector().getInstance(MessageSource.class));
		} else {
			model.values.put("messages", theme.getMessages());
		}
		
		model.values.put("hooks", context.get(HookSystemFeature.class).hookSystem());

		model.values.put("links", new LinkFunction(context));

		model.values.put("PREVIEW_MODE", isPreview(context));
		model.values.put("DEV_MODE", isDevMode(context));
		model.values.put("ENV", context.get(ServerPropertiesFeature.class).serverProperties().env());

		if (context.has(AuthFeature.class)) {
			model.values.put("USERNAME", context.get(AuthFeature.class).username());
		}

		context.get(TemplateHooks.class).getTemplateSupplier().getRegisterTemplateSupplier().forEach(service -> {
			model.values.put(service.name(), service.supplier());
		});
		context.get(TemplateHooks.class).getTemplateFunctions().getRegisterTemplateFunctions().forEach(service -> {
			model.values.put(service.name(), service.function());
		});

		extendModel(model);

		model.values.put("content",
				renderMarkdown(rawContent, context, model)
		);
		
		return templates.get().render((String) meta.get("template"), model);
	}

	protected QueryFunction createQueryFunction(final ReadOnlyFile contentFile, final RequestContext context) {

		Map<String, BiPredicate<Object, Object>> customOperators = new HashMap<>();

		customOperators.putAll( context.get(DBHooks.class).getQueryOperations().getOperations() );

		moduleManager
				.extensions(ContentQueryOperatorExtensionPoint.class)
				.forEach(extension -> customOperators.put(extension.getOperator(), extension.getPredicate()));

		var queryFn = new QueryFunction(db, contentFile, context, customOperators);
		queryFn.setContentType(siteProperties.defaultContentType());
		return queryFn;
	}

	protected NodeListFunctionBuilder createNodeListFunction(final ReadOnlyFile contentFile, final RequestContext context) {
		var nlFn = new NodeListFunctionBuilder(db, contentFile, context);
		nlFn.contentType(siteProperties.defaultContentType());
		return nlFn;
	}

	protected NavigationFunction createNavigationFunction(final ReadOnlyFile contentFile, final RequestContext context) {
		var navFn = new NavigationFunction(db, contentFile, context);
		navFn.contentType(siteProperties.defaultContentType());
		return navFn;
	}

	private boolean isPreview(final RequestContext context) {
		return context.has(IsPreviewFeature.class);
	}

	private boolean isDevMode(final RequestContext context) {
		return context.has(IsDevModeFeature.class);
	}

	private void extendModel(final TemplateEngine.Model model) {
		moduleManager.extensions(TemplateModelExtendingExtentionPoint.class).forEach(extensionPoint -> extensionPoint.extendModel(model));
	}

	@Override
	public Map<String, List<Section>> renderSections(final List<ContentNode> sectionNodes, final RequestContext context) throws IOException {

		if (sectionNodes.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, List<Section>> sections = new HashMap<>();

		final ReadOnlyFile contentBase = db.getReadOnlyFileSystem().contentBase();
		sectionNodes.forEach(node -> {
			try {
				var sectionPath = contentBase.resolve(node.uri());
				var content = render(sectionPath, context);
				var name = SectionUtil.getSectionName(node.name());
				var index = SectionUtil.getSectionIndex(node.name());

				if (!sections.containsKey(name)) {
					sections.put(name, new ArrayList<>());
				}

				sections.get(name).add(new Section(name, index, content));
			} catch (Exception ex) {
				log.error("error render section", ex);
			}

		});

		sections.values().forEach(list -> list.sort((s1, s2) -> Integer.compare(s1.index(), s2.index())));

		return sections;
	}

	private ShortCodeTemplateFunction createShortCodeFunction(RequestContext context) {
		return new ShortCodeTemplateFunction(context.get(RenderContext.class).shortCodes());
	}

}
