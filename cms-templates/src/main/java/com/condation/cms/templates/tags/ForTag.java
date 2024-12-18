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
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.exceptions.TagException;
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
public class ForTag implements Tag {

	@Override
	public String getTagName() {
		return "for";
	}

	@Override
	public Optional<String> getCloseTagName() {
		return Optional.of("endfor");
	}

	@Override
	public void render(TagNode node, Renderer.Context context, Writer writer) {
		var forCondition = parseForLoop(node);

		var colExp = context.engine().createExpression(forCondition.collection);
		
		var collection = colExp.evaluate(context.createEngineContext());
		
		if (collection == null || !(collection instanceof Collection)) {
			throw new TagException("variable '%s' not found".formatted(forCondition.collection), node.getLine(), node.getColumn());
		}

		var index = 0;
		for (var item : (Collection) collection) {
			var loop = new Loop(index++);
			context.scopes().pushScope(
					Map.of(
							"loop", loop,
							forCondition.variable, item
					)
			);
			try {
				for (var child : node.getChildren()) {
					context.renderer().render(child, context, writer);
				}
			} catch (IOException ioe) {
				throw new RenderException(ioe.getMessage(), node.getLine(), node.getColumn());
			} finally {
				context.scopes().popScope();
			}
		}
	}

	private ForDefinition parseForLoop(TagNode node) {
		var loopDefinition = node.getCondition();
		// Überprüfen, ob der String das richtige Format hat
		if (!loopDefinition.contains(" in ")) {
			throw new IllegalArgumentException("Ungültige Schleifendefinition: " + loopDefinition);
		}

		// Extrahiere die Variable und die Collection
		String[] parts = loopDefinition.split(" in ");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Ungültige Schleifendefinition: " + loopDefinition);
		}

		String variable = parts[0].trim();
		String collectionName = parts[1].trim();

		return new ForDefinition(collectionName, variable);
	}

	private static record ForDefinition(String collection, String variable) {

	}

	@RequiredArgsConstructor
	public static class Loop {
		@Getter
		public final int index;
	}
}
