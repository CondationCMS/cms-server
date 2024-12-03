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
import com.condation.cms.templates.parser.ASTNode;
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

public class Renderer {

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
			Map<String, Object> context) {

		public Context (JexlEngine engine,
			ScopeStack scopes,
			RenderFunction renderer,
			CMSTemplateEngine templateEngine) {
			this(engine, scopes, renderer, templateEngine, new HashMap<>());
		}
		
		public ScopeContext createEngineContext() {
			return new ScopeContext(scopes);
		}
	}

	public void render(ASTNode node, final ScopeStack scopes, final Writer writer) throws IOException {
		
		var contentWriter = new StringWriter();
		final Context renderContext = new Context(engine, scopes, this::renderNode, templateEngine);
		renderNode(node, renderContext, contentWriter);
		
		if (renderContext.context().containsKey("_extends")) {
			ExtendsTag.Extends ext = (ExtendsTag.Extends) renderContext.context().get("_extends");
			
			DefaultTemplate parentTemplate = (DefaultTemplate) templateEngine.getTemplate(ext.parentTemplate());
			
			StringWriter parentWriter = new StringWriter();
			renderContext.context().put("_parent", Boolean.TRUE);
			renderNode(parentTemplate.getRootNode(), renderContext, parentWriter);
			
			writer.write(parentWriter.toString());
		} else {
			writer.write(contentWriter.toString());
		}
	}

	private void renderNode(ASTNode node, Context context, Writer writer) throws IOException {

		if (node instanceof TextNode textNode) {
			writer.write(textNode.text);
		} else if (node instanceof VariableNode vnode) {
			renderVariable(vnode, context, writer);
		} else if (node instanceof TagNode tagNode) {
			var tag = configuration.getTag(tagNode.getName());
			if (tag.isPresent()) {
				tag.get().render(tagNode, context, writer);
			}
		} else {
			for (ASTNode child : node.getChildren()) {
				renderNode(child, context, writer);
			}
		}
	}
	
	private void renderVariable (VariableNode node, Context context, Writer writer) {
		variableNodeRenderer.render(node, context, writer);
	}
}
