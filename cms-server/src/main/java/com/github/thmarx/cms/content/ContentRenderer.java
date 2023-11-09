package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.RequestContext;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.extensions.TemplateModelExtendingExtentionPoint;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.template.functions.list.NodeListFunctionBuilder;
import com.github.thmarx.cms.template.functions.navigation.NavigationFunction;
import com.github.thmarx.cms.api.utils.SectionUtil;
import com.github.thmarx.modules.api.ModuleManager;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private final FileSystem fileSystem;
	private final SiteProperties siteProperties;
	private final Supplier<ModuleManager> moduleManager;
	
	public String render (final Path contentFile, final RequestContext context) throws IOException {
		return render(contentFile, context, Collections.emptyMap());
	}
	
	public String render (final Path contentFile, final RequestContext context, final Map<String, List<ContentRenderer.Section>> sections) throws IOException {
		var content = contentParser.parse(contentFile);
		
		var markdownContent = content.content();
		markdownContent = context.renderContext().contentTags().replace(markdownContent);
		
		TemplateEngine.Model model = new TemplateEngine.Model(contentFile);
		model.values.put("meta", content.meta());
		model.values.put("content", context.renderContext().markdownRenderer().render(markdownContent));
		model.values.put("sections", sections);
		
		model.values.put("navigation", new NavigationFunction(this.fileSystem, contentFile, contentParser, context.renderContext().markdownRenderer()));
		model.values.put("nodeList", new NodeListFunctionBuilder(fileSystem, contentFile, contentParser, context.renderContext().markdownRenderer()));
		model.values.put("requestContext", context);
		model.values.put("site", siteProperties);
		
		context.renderContext().extensionHolder().getRegisterTemplateSupplier().forEach(service -> {
			model.values.put(service.name(), service.supplier());
		});

		context.renderContext().extensionHolder().getRegisterTemplateFunctions().forEach(service -> {
			model.values.put(service.name(), service.function());
		});
		
		extendModel(model);
		
		return templates.get().render((String)content.meta().get("template"), model);
	}
	
	private void extendModel (final TemplateEngine.Model model) {
		moduleManager.get().extensions(TemplateModelExtendingExtentionPoint.class).forEach(extensionPoint -> extensionPoint.extendModel(model));
	}
	
	public Map<String, List<Section>> renderSections (final List<MetaData.MetaNode> sectionNodes, final RequestContext context) throws IOException {
		
		if (sectionNodes.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, List<Section>> sections = new HashMap<>();

		final Path contentBase = fileSystem.resolve("content/");
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

	public static record Section (String name, int index, String content) {
		public Section (String name, String content) {
			this(name, Constants.DEFAULT_SECTION_ORDERED_INDEX, content);
		}
	}
}
