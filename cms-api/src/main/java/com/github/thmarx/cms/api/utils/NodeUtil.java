package com.github.thmarx.cms.api.utils;

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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NodeUtil {

	public static Predicate<ContentNode> contentTypeFiler(final String contentType) {
		return (ContentNode node) -> contentType.equals(node.contentType());
	}

	public static String getName(ContentNode node) {

		Map<String, Object> menu = (Map<String, Object>) node.data().getOrDefault(Constants.MetaFields.MENU, Collections.EMPTY_MAP);

		if (menu.containsKey(Constants.MetaFields.MENU_TITLE)) {
			return (String) menu.get(Constants.MetaFields.MENU_TITLE);
		}
		if (node.data().containsKey(Constants.MetaFields.TITLE)) {
			return (String) node.data().get(Constants.MetaFields.TITLE);
		}

		return node.name();
	}

	public static boolean getMenuVisibility(ContentNode node) {

		Map<String, Object> menu = (Map<String, Object>) node.data().getOrDefault(Constants.MetaFields.MENU, Collections.EMPTY_MAP);

		if (menu.containsKey(Constants.MetaFields.MENU_VISIBLE)) {
			return (boolean) menu.get(Constants.MetaFields.MENU_VISIBLE);
		}

		return Constants.DEFAULT_MENU_VISIBILITY;
	}

	public static Double getMenuPosition(ContentNode node) {

		Map<String, Object> menu = (Map<String, Object>) node.data().getOrDefault(Constants.MetaFields.MENU, Collections.EMPTY_MAP);

		if (menu.containsKey(Constants.MetaFields.MENU_POSITION)) {
			var number = (Number) menu.get(Constants.MetaFields.MENU_POSITION);
			return number.doubleValue();
		}

		return Constants.DEFAULT_MENU_POSITION;
	}

	public static String excerpt(final ContentNode node, final String markdown, final int length, final MarkdownRenderer renderer) {
		if (node.hasMetaValue(Constants.MetaFields.EXCERPT)) {
			return elipsis(node.getMetaValue(Constants.MetaFields.EXCERPT, ""), length);
		}
		return renderer.excerpt(markdown, length);
	}

	public static String elipsis(final String text, final int length) {
		return text.codePoints()
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
}
