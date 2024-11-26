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
			Object variableValue = vnode.getExpression().evaluate(scopeContext);
			if (variableValue != null && variableValue instanceof String stringValue) {
				output.append(StringEscapeUtils.ESCAPE_HTML4.translate(stringValue));
			} else {
				output.append(variableValue != null ? variableValue : "");
			}

		} else if (node instanceof TagNode tagNode) {
			var tag = configuration.getTag(tagNode.getName());
			if (tag.isPresent()) {
				tag.get().render(tagNode, context, output);
			}
			/*
            TagNode tagNode = (TagNode) node;
            // Beispiel: Wir unterstützen das "if"-Tag
            if ("if".equals(tagNode.getName())) {
                ASTNode conditionNode = tagNode.getChildren().get(0);
                if (conditionNode instanceof VariableNode) {
                    String variableValue = context.getVariable(((VariableNode) conditionNode).getVariable());
                    if (variableValue != null && !variableValue.isEmpty()) {
                        for (int i = 1; i < tagNode.getChildren().size(); i++) {
                            renderNode(tagNode.getChildren().get(i), output);
                        }
                    }
                }
            } else {
                // Anderes Tag-Verhalten kann hier hinzugefügt werden
                for (ASTNode child : tagNode.getChildren()) {
                    renderNode(child, engine, scopes, output);
                }
            }
			 */
		} else {
			for (ASTNode child : node.getChildren()) {
				renderNode(child, context, output);
			}
		}
	}
}
