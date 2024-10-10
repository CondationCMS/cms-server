package com.condation.cms.content.shortcodes;

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
import com.condation.cms.api.model.Parameter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

@Slf4j
public class ShortCodeParser {

	public static final String SHORTCODE_REGEX = "\\[\\[(\\w+)([^\\]]*)\\]\\](.*?)\\[\\[\\/\\1\\]\\]|\\[\\[(\\w+)([^\\]]*)\\s*\\/\\]\\]";
	public static final Pattern SHORTCODE_PATTERN = Pattern.compile(SHORTCODE_REGEX, Pattern.DOTALL);
	//public static final Pattern PARAM_PATTERN = Pattern.compile("(\\w+)=(\"[^\"]*\"|'[^']*')");
	public static final Pattern PARAM_PATTERN = Pattern.compile("(\\w+)=((\"[^\"]*\"|'[^']*'|\\[[^\\]]*\\]))");

	public ShortCodeParser() {
	}

	public List<Match> parseShortcodes(String text) {
		List<Match> shortcodes = new ArrayList<>();
		Matcher matcher = SHORTCODE_PATTERN.matcher(text);

		while (matcher.find()) {
			String name = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
			String params = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(5).trim();
			String content = matcher.group(3) != null ? matcher.group(3).trim() : "";

			Match match = new Match(name, matcher.start(), matcher.end());
			match.setContent(content);
			match.getParameters().put("content", content);

			Matcher paramMatcher = PARAM_PATTERN.matcher(params);

			while (paramMatcher.find()) {
				String key = paramMatcher.group(1);
				String value = paramMatcher.group(2);
				value = value.substring(1, value.length() - 1); // Entfernt die Anführungszeichen oder Klammern bei Arrays
				match.getParameters().put(key, value);
			}

			shortcodes.add(match);
		}

		return shortcodes;
	}

	public String replace(String content, Codes codes) {
		StringBuilder newContent = new StringBuilder();
		int lastPosition = 0;
		var matches = parseShortcodes(content);

		for (var match : matches) {
			newContent.append(content, lastPosition, match.getStart());

			try {
				newContent.append(codes.get(match.getName()).apply(match.getParameters()));
			} catch (Exception e) {
				log.error("error executing shortcode", e);
			}

			lastPosition = match.getEnd();
		}

		if (content.length() > lastPosition) {
			newContent.append(content.substring(lastPosition));
		}

		return newContent.toString();
	}

	@RequiredArgsConstructor
	@Getter
	public static class Match {

		private final String name;
		private final int start;
		private final int end;
		private Parameter parameters = new Parameter();

		@Setter
		private String content;
	}

	public static class Codes {

		private Map<String, Function<Parameter, String>> codes = new HashMap<>();

		public void addAll(Map<String, Function<Parameter, String>> codes) {
			this.codes.putAll(codes);
		}

		public void add(final String codeName, Function<Parameter, String> function) {
			codes.put(codeName, function);
		}

		public Function<Parameter, String> get(final String codeName) {
			return codes.getOrDefault(codeName, (params) -> "");
		}
	}
}
