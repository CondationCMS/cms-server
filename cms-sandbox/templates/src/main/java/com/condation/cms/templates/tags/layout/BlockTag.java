package com.condation.cms.templates.tags.layout;

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
import com.condation.cms.templates.parser.TagNode;
import com.condation.cms.templates.renderer.Renderer;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class BlockTag implements Tag {

	@Override
	public String getTagName() {
		return "block";
	}

	@Override
	public Optional<String> getCloseTagName() {
		return Optional.of("endblock");
	}

	@Override
	public void render(TagNode node, Renderer.Context context, StringBuilder sb) {
		if (context.context().containsKey("_parent")) {
			handleParent(node, context, sb);
		} else {
			handleChild(node, context);
		}
	}

	private String getBlockKey(TagNode node) {
		var blockName = node.getCondition().trim();
		return "_block_%s".formatted(blockName);
	}

	private void handleParent(TagNode node, Renderer.Context context, StringBuilder sb) {
		var blockKey = getBlockKey(node);
		if (context.context().containsKey(blockKey)) {
			sb.append(context.context().get(blockKey));
		} else {
			for (var child : node.getChildren()) {
				context.renderer().render(child, context, sb);
			}
		}
	}

	private void handleChild(TagNode node, Renderer.Context context) {
		StringBuilder sb = new StringBuilder();
		for (var child : node.getChildren()) {
			context.renderer().render(child, context, sb);
		}

		context.context().put(
				getBlockKey(node),
				sb.toString());
	}
}
