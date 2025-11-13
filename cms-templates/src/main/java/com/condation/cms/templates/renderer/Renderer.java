package com.condation.cms.templates.renderer;

/*-
 * #%L
 * templates
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
import com.condation.cms.templates.DefaultTemplate;
import com.condation.cms.templates.RenderFunction;
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.DynamicConfiguration;
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.ComponentNode;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.TextNode;
import com.condation.cms.templates.parser.VariableNode;
import com.condation.cms.templates.tags.layout.ExtendsTag;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jexl3.JexlEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Renderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);
	private static final int MAX_DEPTH = 100;

	private final TemplateConfiguration configuration;
	private final CMSTemplateEngine templateEngine;
	private final JexlEngine engine;
	private final VariableNodeRenderer variableNodeRenderer;

	public Renderer(TemplateConfiguration configuration, CMSTemplateEngine templateEngine, JexlEngine engine) {
		this.configuration = configuration;
		this.templateEngine = templateEngine;
		this.engine = engine;
		this.variableNodeRenderer = new VariableNodeRenderer(configuration);
	}

	public static record Context(
			JexlEngine engine,
			ScopeStack scopes,
			RenderFunction renderer,
			CMSTemplateEngine templateEngine,
			Map<String, Object> context,
			DynamicConfiguration dynamicConfiguration) {

		public Context(JexlEngine engine,
				ScopeStack scopes,
				RenderFunction renderer,
				CMSTemplateEngine templateEngine,
				DynamicConfiguration dynamicConfiguration) {
			this(engine, scopes, renderer, templateEngine, new HashMap<>(), dynamicConfiguration);
		}

		public ScopeContext createEngineContext() {
			return new ScopeContext(scopes);
		}
	}

	public void render(ASTNode node, final ScopeStack scopes, final Writer writer, final DynamicConfiguration dynamicConfiguration) throws IOException {
		var renderConfig = new RenderConfiguration(configuration, dynamicConfiguration);
		var renderFunction = (RenderFunction) (ASTNode node1, Context context, Writer writer1) -> {
			renderNode(node1, context, writer1, renderConfig, 0);
		};

		var contentWriter = new StringWriter();
		final Context renderContext = new Context(
				engine,
				scopes,
				renderFunction,
				templateEngine,
				dynamicConfiguration);
		renderFunction.render(node, renderContext, contentWriter);

		if (renderContext.context().containsKey("_extends")) {
			ExtendsTag.Extends ext = (ExtendsTag.Extends) renderContext.context().get("_extends");
			DefaultTemplate parentTemplate = (DefaultTemplate) templateEngine.getTemplate(ext.parentTemplate());
			StringWriter parentWriter = new StringWriter();
			renderContext.context().put("_parent", Boolean.TRUE);
			renderFunction.render(parentTemplate.getRootNode(), renderContext, parentWriter);
			writer.write(parentWriter.toString());
		} else {
			writer.write(contentWriter.toString());
		}
	}

	private void renderNode(ASTNode node, Context context, Writer writer, RenderConfiguration renderConfiguration, int depth) throws IOException {
		if (depth > MAX_DEPTH) {
			throw new RenderException("Maximum render depth exceeded", node.getLine(), node.getColumn());
		}

		switch (node) {
			case TextNode textNode -> writer.write(textNode.text);
			case VariableNode vnode -> renderVariable(vnode, context, writer);
			case TagNode tagNode -> {
				var tag = renderConfiguration.getTag(tagNode.getName());
				if (tag.isPresent()) {
					tag.get().render(tagNode, context, writer);
				} else {
					handleUnknown("tag", tagNode.getName(), node);
				}
			}
			case ComponentNode componentNode -> {
				var component = renderConfiguration.getComponent(componentNode.getName());
				if (component.isPresent()) {
					component.get().render(componentNode, context, writer);
				} else {
					handleUnknown("component", componentNode.getName(), node);
				}
			}
			default -> {
				for (ASTNode child : node.getChildren()) {
					renderNode(child, context, writer, renderConfiguration, depth + 1);
				}
			}
		}
	}

	private void handleUnknown(String type, String name, ASTNode node) {
		if (this.configuration.isDevMode()) {
			throw new RenderException("unknown " + type + ": " + name, node.getLine(), node.getColumn());
		} else {
			LOGGER.warn("unknown " + type + " '{}' at L{}C{}", name, node.getLine(), node.getColumn());
		}
	}

	private void renderVariable(VariableNode node, Context context, Writer writer) {
		variableNodeRenderer.render(node, context, writer);
	}
}
