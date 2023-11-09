package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class ContentTags {

	public static final Pattern TAG_PARAMS_PATTERN_SHORT = Pattern.compile("\\[{2}(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\p{Blank}*/\\]{2}");
	
	public static final Pattern TAG_PARAMS_PATTERN_LONG = Pattern.compile("\\[{2}(?<tag>[a-z_A-Z0-9]+)( (?<params>.*?))?\\]{2}(?<content>.*)\\[{2}/\\k<tag>\\]{2}");
	
	private final Tags tags;

	public ContentTags (Map<String, Function<Parameter, String>> tags) {
		this.tags = new Tags();
		this.tags.addAll(tags);
	}
	
	public String replace (final String content) {
		
		var newContent = _replace(content, TAG_PARAMS_PATTERN_SHORT);
		return _replace(newContent, TAG_PARAMS_PATTERN_LONG);
	}
	
	private String _replace (final String content, final Pattern pattern) {
		var matcher = pattern.matcher(content);

		String newContent = "";
		int lastPosition = 0;
		while (matcher.find(lastPosition)) {
			var tagName = matcher.group("tag");

			newContent += content.substring(lastPosition, matcher.start());
			Parameter params = parseParameters(matcher.group("params"));
			if (matcher.namedGroups().containsKey("content")) {
				params.put("content", matcher.group("content"));
			}
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
	
	public static class Tags {
		private Map<String, Function<Parameter, String>> tags = new HashMap<>();
		
		public void addAll(Map<String, Function<Parameter, String>> tags) {
			this.tags.putAll(tags);
		}
		
		public void add (final String tagName, Function<Parameter, String> function) {
			tags.put(tagName, function);
		}
		public Function<Parameter, String> get (final String tagName) {
			return tags.getOrDefault(tagName, (params) -> "");
		}
	}
	
	public static class Parameter extends HashMap<String, Object> {
	}
}
