package com.condation.cms.content.markdown.rules.block;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.content.markdown.Block;
import com.condation.cms.content.markdown.BlockElementRule;
import com.condation.cms.content.markdown.InlineRenderer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class ListBlockRule implements BlockElementRule {

	private static final Pattern PATTERN_ORDERED_LIST = Pattern.compile(
			"^[0-9]+\\. (.+?)((^\n)|\\Z)",
			Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern PATTERN_UNORDERED_LIST = Pattern.compile(
			"^[-\\*\\+]{1} (.+?)((^\n)|\\Z)",
			Pattern.MULTILINE | Pattern.DOTALL);

	@Override
	public Block next(String md) {
		Matcher matcher = PATTERN_ORDERED_LIST.matcher(md);
		if (matcher.find()) {
			var listContent = matcher.group(0);
			
			var items = listContent.split("(^|\n)[0-9]?\\. ");

			var listItems = Stream.of(items).skip(1).toList();

			return new ListBlock(matcher.start(), matcher.end(),
					listItems, true);
		} else {
			matcher = PATTERN_UNORDERED_LIST.matcher(md);
			if (matcher.find()) {
				var listContent = matcher.group(0);
				var items = listContent.split("(^|\n)[-\\*\\+]{1} ");

				var listItems = Stream.of(items).skip(1).toList();

				return new ListBlock(matcher.start(), matcher.end(),
						listItems, false);
			}
		}
		return null;
	}

	public static record ListBlock(int start, int end, List<String> items, boolean ordered) implements Block {

		@Override
		public String render(InlineRenderer inlineRenderer) {
			if (ordered) {
				return "<ol>%s</ol>".formatted(
						items.stream().map(item -> "<li>" + inlineRenderer.render(item) + "</li>").collect(Collectors.joining()));
			} else {
				return "<ul>%s</ul>".formatted(
						items.stream().map(item -> "<li>" + inlineRenderer.render(item) + "</li>").collect(Collectors.joining()));
			}
		}

	}

}
