package com.condation.cms.content;

import com.condation.cms.api.Constants;
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
import com.condation.cms.api.extensions.TemplateModelExtendingExtensionPoint;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.MarkdownRendererFeature;
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
import com.condation.cms.api.content.MapAccess;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.extensions.hooks.DBHooks;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.content.template.functions.LinkFunction;
import com.condation.cms.content.template.functions.MarkdownFunction;
import com.condation.cms.content.template.functions.list.NodeListFunctionBuilder;
import com.condation.cms.content.template.functions.navigation.NavigationFunction;
import com.condation.cms.content.template.functions.query.QueryFunction;
import com.condation.cms.content.template.functions.tag.TagTemplateFunction;
import com.condation.cms.content.template.functions.taxonomy.TaxonomyFunction;
import com.condation.cms.content.template.functions.translation.NodeTranslations;
import com.condation.cms.content.template.functions.translation.SiteTranslations;
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

	private String renderContent(final String rawContent, final RequestContext context, final TemplateEngine.Model model) {
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

		TemplateEngine.Model model = new TemplateEngine.Model(
				contentFile, 
				contentNode.orElse(null),
				context);

		modelExtending.accept(model);

		Namespace namespace = new Namespace();

		namespace.add(Constants.TemplateNamespaces.NODE, "meta", new MapAccess(meta));
		namespace.add(Constants.TemplateNamespaces.NODE, "sections", sections);
		namespace.add(Constants.TemplateNamespaces.NODE, "uri", uri);
		namespace.add(Constants.TemplateNamespaces.NODE, "translation", new NodeTranslations(contentNode.orElse(null), siteProperties));
		
		var canonicalUrl = "";
		if (contentNode.isPresent()) {
			canonicalUrl = PathUtil.toURL(contentNode.get().uri());
			canonicalUrl = HTTPUtil.modifyUrl(canonicalUrl, siteProperties);
		}
		namespace.add(Constants.TemplateNamespaces.NODE, "canonicalUrl", canonicalUrl);

		TagTemplateFunction tagFunction = createTagFunction(context);
		namespace.add(Constants.TemplateNamespaces.CMS, TagTemplateFunction.KEY, tagFunction);
		
		NavigationFunction navigationFunction = createNavigationFunction(contentFile, context);
		namespace.add(Constants.TemplateNamespaces.CMS, "navigation", navigationFunction);
		
		NodeListFunctionBuilder nodeListFunction = createNodeListFunction(contentFile, context);
		namespace.add(Constants.TemplateNamespaces.CMS, "nodeList", nodeListFunction);
		
		QueryFunction queryFunction = createQueryFunction(contentFile, context);
		namespace.add(Constants.TemplateNamespaces.CMS, "query", queryFunction);
		
		MarkdownFunction markdownFunction = createMarkdownFunction(context);
		namespace.add(Constants.TemplateNamespaces.CMS, "markdown", markdownFunction);

		model.values.put("requestContext", context.get(RequestFeature.class));
		model.values.put("theme", context.get(RenderContext.class).theme());
		namespace.add(Constants.TemplateNamespaces.CMS, "mediaService", context.get(SiteMediaServiceFeature.class).mediaService());
		namespace.add(Constants.TemplateNamespaces.CMS, "taxonomies", context.get(InjectorFeature.class).injector().getInstance(TaxonomyFunction.class));

		namespace.add(Constants.TemplateNamespaces.SITE, "properties", siteProperties);
		namespace.add(Constants.TemplateNamespaces.SITE, "translation", new SiteTranslations(siteProperties));
		
		var theme = context.get(RenderContext.class).theme();
		if (theme.empty()) {
			model.values.put("messages", context.get(InjectorFeature.class).injector().getInstance(MessageSource.class));
		} else {
			model.values.put("messages", theme.getMessages());
		}
		
		namespace.add(Constants.TemplateNamespaces.CMS, "hooks", context.get(HookSystemFeature.class).hookSystem());

		namespace.add(Constants.TemplateNamespaces.CMS, "links", new LinkFunction(context));

		model.values.put("PREVIEW_MODE", isPreview(context));
		model.values.put("MANAGER", isManager(context));
		model.values.put("DEV_MODE", isDevMode(context));
		model.values.put("ENV", context.get(ServerPropertiesFeature.class).serverProperties().env());

		if (context.has(AuthFeature.class)) {
			model.values.put("USERNAME", context.get(AuthFeature.class).username());
		}

		context.get(TemplateHooks.class).getTemplateSupplier().getRegisterTemplateSupplier().forEach(service -> {
			model.values.put(service.name(), service.supplier());
			namespace.add(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, service.name(), service.supplier());
		});

		extendModel(model, namespace);

		var modelCopy = model.copy();
		modelCopy.values.putAll(namespace.getNamespaces());
		
		String content = renderContent(rawContent, context, modelCopy);
		model.values.put("content", content);
		namespace.add(Constants.TemplateNamespaces.NODE, "content", content);
		
		model.values.putAll(namespace.getNamespaces());

		return templates.get().render((String) meta.get("template"), model);
	}

	protected MarkdownFunction createMarkdownFunction(final RequestContext context) {
		return new MarkdownFunction(context.get(MarkdownRendererFeature.class).markdownRenderer());
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

	private boolean isManager(final RequestContext context) {
		if (context.has(IsPreviewFeature.class))  {
			return context.get(IsPreviewFeature.class).mode().equals(IsPreviewFeature.Mode.MANAGER);
		}
		return false;
	}
	
	private boolean isDevMode(final RequestContext context) {
		return context.has(IsDevModeFeature.class);
	}

	private void extendModel(final TemplateEngine.Model model, Namespace namespace) {
		moduleManager.extensions(TemplateModelExtendingExtensionPoint.class).forEach(extensionPoint -> {
			var modModel = extensionPoint.getModel();
			// deprecated: module extensions on root will be remove in 8.0.0
			model.values.putAll(modModel);
			modModel.entrySet().forEach(entry -> namespace.add(
				extensionPoint.getNamespace(), 
				entry.getKey(), 
				entry.getValue()
			));
		});
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
				var index = node.getMetaValue(Constants.MetaFields.LAYOUT_ORDER, Constants.DEFAULT_SECTION_LAYOUT_ORDER);

				if (!sections.containsKey(name)) {
					sections.put(name, new ArrayList<>());
				}

				sections.get(name).add(new Section(name, index, content, node.data()));
			} catch (Exception ex) {
				log.error("error render section", ex);
			}

		});

		sections.values().forEach(list -> list.sort((s1, s2) -> Integer.compare(s1.index(), s2.index())));

		return sections;
	}

	private TagTemplateFunction createTagFunction(RequestContext context) {
		return new TagTemplateFunction(context, context.get(RenderContext.class).tags());
	}

}
