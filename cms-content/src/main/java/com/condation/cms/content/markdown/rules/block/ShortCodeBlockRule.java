package com.condation.cms.content.markdown.rules.block;

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
import com.condation.cms.content.markdown.Block;
import com.condation.cms.content.markdown.BlockElementRule;
import com.condation.cms.content.markdown.InlineRenderer;
import com.condation.cms.content.shortcodes.ShortCodeMap;
import com.condation.cms.content.shortcodes.ShortCodeParser;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class ShortCodeBlockRule implements BlockElementRule {

	private static final ShortCodeParser tagParser = new ShortCodeParser(null);

	@Override
	public Block next(final String md) {

		List<ShortCodeParser.ShortCodeInfo> shortCodes = tagParser.findShortCodes(md, new ShortCodeMap() {
			@Override
			public boolean has(String codeName) {
				return true;
			}
		}).stream()
				.filter(tag -> isStandaloneInLine(md, tag))
				.toList();
		if (shortCodes.isEmpty()) {
			return null;
		}
		var shortcode = shortCodes.getFirst();
		return new ShortCodeBlock(
				shortcode.startIndex(),
				shortcode.endIndex(),
				shortcode);

	}

	public static record ShortCodeBlock(int start, int end, ShortCodeParser.ShortCodeInfo shortCodeInfo) implements Block {

		@Override
		public String render(InlineRenderer inlineRenderer, int documentOffset) {
			List<String> params = shortCodeInfo.rawAttributes()
					.entrySet().stream()
					.filter(entry -> !entry.getKey().equals("_content"))
					.sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
					.map(entry -> {
						return "%s=%s".formatted(entry.getKey(), parseValue((String) entry.getValue()));
					}).toList();
			return "[[%s %s]]%s[[/%s]]"
					.formatted(shortCodeInfo.name(),
							String.join(" ", params),
							//inlineRenderer.render((String)shortCodeInfo.rawAttributes().getOrDefault("_content", ""), documentOffset),
							shortCodeInfo.rawAttributes().getOrDefault("_content", ""),
                            shortCodeInfo.name()
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

	public boolean isStandaloneInLine(String text, ShortCodeParser.ShortCodeInfo tag) {
		var startIndex = tag.startIndex();
		var endIndex = tag.endIndex();
		// Prüfe, ob die Indizes gültig sind
		if (startIndex < 0 || endIndex > text.length() || startIndex >= endIndex) {
			throw new IllegalArgumentException("Ungültige Indizes");
		}

		// Finde die Position des Textausschnitts
		String before = text.substring(0, startIndex);
		String after = text.substring(endIndex);

		// Prüfe, ob vor und nach dem Ausschnitt ein Zeilenumbruch oder nichts steht
		boolean beforeIsLineBreak = before.isEmpty() || before.endsWith("\n") || before.endsWith("\r\n");
		boolean afterIsLineBreak = after.isEmpty() || after.startsWith("\n") || after.startsWith("\r\n");

		return beforeIsLineBreak && afterIsLineBreak;
	}

}
