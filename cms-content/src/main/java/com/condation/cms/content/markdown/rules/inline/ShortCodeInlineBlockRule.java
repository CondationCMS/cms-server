package com.condation.cms.content.markdown.rules.inline;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.InlineElementRule;
import com.condation.cms.content.markdown.InlineElementTokenizer;
import com.condation.cms.content.shortcodes.ShortCodeMap;
import com.condation.cms.content.shortcodes.ShortCodeParser;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class ShortCodeInlineBlockRule implements InlineElementRule {

	private static final ShortCodeParser shortCodeParser = new ShortCodeParser(null);
	
	@Override
	public InlineBlock next(InlineElementTokenizer tokenizer, final String md) {

		List<ShortCodeParser.ShortCodeInfo> shortCodes = shortCodeParser.findShortCodes(md, new ShortCodeMap() {
			@Override
			public boolean has(String codeName) {
				return true;
			}
		}).stream().toList();
		if (shortCodes.isEmpty()) {
			return null;
		}
		var shortCode = shortCodes.getFirst();
		return new ShortCodeInlineBlock(
				shortCode.startIndex(),
				shortCode.endIndex(),
				shortCode);
	}

	public static record ShortCodeInlineBlock(int start, int end, ShortCodeParser.ShortCodeInfo shortCodeInfo) implements InlineBlock {

		@Override
		public String render() {
			List<String> params = shortCodeInfo.rawAttributes()
					.entrySet().stream()
					.filter(entry -> !entry.getKey().equals("_content"))
					.sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
					.map(entry -> {
						return "%s=%s".formatted(entry.getKey(),
								parseValue((String) entry.getValue(),
										ShortCodeParser.isQuotedAttribute(shortCodeInfo, entry.getKey())));
					}).toList();
			return "[[%s %s]]%s[[/%s]]"
					.formatted(shortCodeInfo.name(),
							String.join(" ", params),
							shortCodeInfo.rawAttributes().getOrDefault("_content", ""),
							shortCodeInfo.name()
					);
		}

	}

	private static Object parseValue(String value, boolean quoted) {
		if (quoted) {
			return "\"" + value + "\"";
		}
		if (value.matches("\\d+")) {
			return value;
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return value;
		}
		return "\"" + value + "\"";
	}
}
