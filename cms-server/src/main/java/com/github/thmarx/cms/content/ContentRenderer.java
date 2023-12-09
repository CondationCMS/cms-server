package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.Startup;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.extensions.TemplateModelExtendingExtentionPoint;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.request.features.IsDevModeFeature;
import com.github.thmarx.cms.api.request.features.IsPreviewFeature;
import com.github.thmarx.cms.api.request.features.RequestFeature;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.github.thmarx.cms.filesystem.functions.list.NodeListFunctionBuilder;
import com.github.thmarx.cms.filesystem.functions.navigation.NavigationFunction;
import com.github.thmarx.cms.api.utils.SectionUtil;
import com.github.thmarx.cms.filesystem.functions.query.QueryFunction;
import com.github.thmarx.cms.request.RenderContext;
import com.github.thmarx.cms.request.RequestExtensions;
import com.github.thmarx.modules.api.ModuleManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class ContentRenderer {

	private final ContentParser contentParser;
	private final Supplier<TemplateEngine> templates;
	private final DB db;
	private final SiteProperties siteProperties;
	private final Supplier<ModuleManager> moduleManager;

	public String render(final Path contentFile, final RequestContext context) throws IOException {
		return render(contentFile, context, Collections.emptyMap());
	}

	public String render(final Path contentFile, final RequestContext context, final Map<String, List<ContentRenderer.Section>> sections) throws IOException {
		var content = contentParser.parse(contentFile);

		var markdownContent = content.content();
		markdownContent = context.get(RenderContext.class).contentTags().replace(markdownContent);

		var uri = PathUtil.toRelativeFile(contentFile, db.getFileSystem().resolve("content/"));
		Optional<ContentNode> contentNode = db.getContent().byUri(uri);

		TemplateEngine.Model model = new TemplateEngine.Model(contentFile, contentNode.isPresent() ? contentNode.get() : null);
		model.values.put("meta", content.meta());
		model.values.put("content", context.get(RenderContext.class).markdownRenderer().render(markdownContent));
		model.values.put("sections", sections);

		model.values.put("navigation", new NavigationFunction(db, contentFile, contentParser, context.get(RenderContext.class).markdownRenderer()));
		model.values.put("nodeList", new NodeListFunctionBuilder(db, contentFile, contentParser, context.get(RenderContext.class).markdownRenderer()));
		model.values.put("query", new QueryFunction(db, contentFile, contentParser, context.get(RenderContext.class).markdownRenderer()));
		model.values.put("requestContext", context.get(RequestFeature.class));
		model.values.put("theme", context.get(RenderContext.class).theme());
		model.values.put("site", siteProperties);

		model.values.put("PREVIEW_MODE", isPreview(context));
		model.values.put("DEV_MODE", isDevMode(context));

		context.get(RequestExtensions.class).getRegisterTemplateSupplier().forEach(service -> {
			model.values.put(service.name(), service.supplier());
		});

		context.get(RequestExtensions.class).getRegisterTemplateFunctions().forEach(service -> {
			model.values.put(service.name(), service.function());
		});

		extendModel(model);

		return templates.get().render((String) content.meta().get("template"), model);
	}

	private boolean isPreview(final RequestContext context) {
		if (context.has(IsPreviewFeature.class)) {
			return context.get(IsPreviewFeature.class).isPreview();
		}
		return false;
	}
	private boolean isDevMode(final RequestContext context) {
		if (context.has(IsDevModeFeature.class)) {
			return context.get(IsDevModeFeature.class).isDevMode();
		}
		return false;
	}

	private void extendModel(final TemplateEngine.Model model) {
		moduleManager.get().extensions(TemplateModelExtendingExtentionPoint.class).forEach(extensionPoint -> extensionPoint.extendModel(model));
	}

	public Map<String, List<Section>> renderSections(final List<ContentNode> sectionNodes, final RequestContext context) throws IOException {

		if (sectionNodes.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, List<Section>> sections = new HashMap<>();

		final Path contentBase = db.getFileSystem().resolve("content/");
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

		sections.values().forEach(list -> list.sort((s1, s2) -> Integer.compare(s1.index, s2.index)));

		return sections;
	}

	public static record Section(String name, int index, String content) {

		public Section(String name, String content) {
			this(name, Constants.DEFAULT_SECTION_ORDERED_INDEX, content);
		}
	}
}
