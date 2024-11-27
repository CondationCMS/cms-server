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
import com.condation.cms.templates.RenderFunction;
import com.condation.cms.templates.TemplateConfiguration;
import com.condation.cms.templates.TemplateEngine;
import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.parser.TextNode;
import com.condation.cms.templates.parser.VariableNode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.text.StringEscapeUtils;

@RequiredArgsConstructor
public class Renderer {

	private final TemplateConfiguration configuration;
	private final TemplateEngine templateEngine;
	
	private final VariableNodeRenderer variableNodeRenderer = new VariableNodeRenderer();

	public static record Context(
			JexlEngine engine,
			ScopeStack scopes,
			RenderFunction renderer,
			TemplateEngine templateEngine) {

		public ScopeContext createEngineContext() {
			return new ScopeContext(scopes);
		}
	}

	public String render(ASTNode node, final JexlEngine engine, final ScopeStack scopes) {
		StringBuilder output = new StringBuilder();
		renderNode(node, new Context(engine, scopes, this::renderNode, templateEngine), output);
		return output.toString();
	}

	private void renderNode(ASTNode node, Context context, StringBuilder output) {

		var scopeContext = context.createEngineContext();

		if (node instanceof TextNode textNode) {
			output.append(textNode.text);
		} else if (node instanceof VariableNode vnode) {
			renderVariable(vnode, scopeContext, output);
		} else if (node instanceof TagNode tagNode) {
			var tag = configuration.getTag(tagNode.getName());
			if (tag.isPresent()) {
				tag.get().render(tagNode, context, output);
			}
		} else {
			for (ASTNode child : node.getChildren()) {
				renderNode(child, context, output);
			}
		}
	}
	
	private void renderVariable (VariableNode node, ScopeContext context, StringBuilder output) {
		variableNodeRenderer.render(node, context, output);
	}
}
