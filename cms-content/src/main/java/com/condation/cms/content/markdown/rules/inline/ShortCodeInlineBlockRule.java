package com.condation.cms.content.markdown.rules.inline;

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
import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import com.condation.cms.content.shortcodes.TagMap;
import com.condation.cms.content.shortcodes.TagParser;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class ShortCodeInlineBlockRule implements InlineElementRule {

	private static final TagParser tagParser = new TagParser(null);
	
	@Override
	public InlineBlock next(final String md) {

		List<TagParser.TagInfo> tags = tagParser.findTags(md, new TagMap() {
			@Override
			public boolean has(String codeName) {
				return true;
			}
		}).stream().toList();
		if (tags.isEmpty()) {
			return null;
		}
		var tag = tags.getFirst();
		return new ShortCodeInlineBlock(
				tag.startIndex(),
				tag.endIndex(),
				tag);
	}

	public static record ShortCodeInlineBlock(int start, int end, TagParser.TagInfo tagInfo) implements InlineBlock {

		@Override
		public String render() {
			List<String> params = tagInfo.rawAttributes()
					.entrySet().stream()
					.filter(entry -> !entry.getKey().equals("_content"))
					.sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
					.map(entry -> {
						return "%s=%s".formatted(entry.getKey(), parseValue((String) entry.getValue()));
					}).toList();
			return "[[%s %s]]%s[[/%s]]"
					.formatted(
							tagInfo.name(),
							String.join(" ", params),
							tagInfo.rawAttributes().getOrDefault("_content", ""),
							tagInfo.name()
					);
		}

	}

	private static Object parseValue(String value) {
		if (value.matches("\\d+")) {
			return value;
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return value;
		}
		return "\"" + value + "\"";
	}
}
