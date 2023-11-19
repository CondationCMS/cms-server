package com.github.thmarx.cms.markdown.home;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

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
/**
 *
 * @author t.marx
 */
public class TagReplacer {

	public static Pattern TAG_PARAMS_PATTERN = Pattern.compile("\\[{2}(?<tag>.*?)( (?<params>.*?))?\\]{2}");

	private Map<String, Function<Parameter, String>> tags = new HashMap<>();

	public TagReplacer() {
	}
	
	public void add (String tagname, Function<Parameter, String> function) {
		tags.put(tagname, function);
	}

	public String replace(final String content) {

		var matcher = TAG_PARAMS_PATTERN.matcher(content);

		String newContent = "";
		int lastPosition = 0;
		while (matcher.find()) {
			var tagName = matcher.group("tag");

			if (!tags.containsKey(tagName)) {
				continue;
			}

			newContent += content.substring(lastPosition, matcher.start());
			Parameter params = parseParameters(matcher.group("params"));
			newContent += tags.get(tagName).apply(params);

			lastPosition = matcher.end();
		}
		if (content.length() > lastPosition) {
			newContent += content.substring(lastPosition);
		}

		return newContent;
	}

	private Parameter parseParameters(final String paramString) {
		Parameter params = new Parameter();

		if (Strings.isNullOrEmpty(paramString)) {
			return params;
		}

		Map<String, String> result = Splitter.on(',')
				.trimResults()
				.withKeyValueSeparator(
						Splitter.on('=')
								.limit(2)
								.trimResults(CharMatcher.anyOf("'\" ")))
				.split(paramString);

		params.putAll(result);

		return params;
	}

	public static class Parameter extends HashMap<String, Object> {
	}
}
