package com.condation.cms.templates.tags;

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
import com.condation.cms.templates.Tag;
import com.condation.cms.templates.parser.ASTNode;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.jexl3.JexlExpression;

/**
 *
 * @author t.marx
 */
public class IfTag implements Tag {

	@Override
	public String getTagName() {
		return "if";
	}

	@Override
	public Optional<String> getCloseTagName() {
		return Optional.of("endif");
	}

	@Override
	public void render(TagNode node, Renderer.Context context, StringBuilder sb) {
		List<Condition> conditions = buildConditions(node);

		var scopeContext = context.createEngineContext();
		for (Condition condition : conditions) {
			try {
				context.scopes().pushScope();
				if (condition.expression() != null) {
					Object value = condition.expression().evaluate(scopeContext);
					if (value instanceof Boolean boolValue && boolValue == true) {
						for (var child : condition.currentChildren) {
							context.renderer().render(child, context, sb);
						}
						break;
					}
				} else {
					for (var child : condition.currentChildren) {
						context.renderer().render(child, context, sb);
					}
					break;
				}
			} finally {
				context.scopes().popScope();
			}
		}
	}

	private List<Condition> buildConditions(TagNode node) {

		List<Condition> conditions = new ArrayList<>();

		TagNode currentTag = node;
		List<ASTNode> currentChildren = new ArrayList<>();
		for (var child : node.getChildren()) {
			if (child instanceof TagNode tagNode) {
				if ("elseif".equals(tagNode.getName())) {
					conditions.add(new Condition(currentTag.getName(), currentTag.getExpression(), currentChildren));
					currentTag = tagNode;
					currentChildren = new ArrayList<>();
				} else if ("else".equals(tagNode.getName())) {
					conditions.add(new Condition(currentTag.getName(), currentTag.getExpression(), currentChildren));
					currentTag = tagNode;
					currentChildren = new ArrayList<>();
				} else if ("endif".equals(tagNode.getName())) {
					conditions.add(new Condition(currentTag.getName(), currentTag.getExpression(), currentChildren));
				} else {
					currentChildren.add(child);
				}
			} else {
				currentChildren.add(child);
			}
		}

		return conditions;
	}

	private static record Condition(String name, JexlExpression expression, List<ASTNode> currentChildren) {

	}
}
