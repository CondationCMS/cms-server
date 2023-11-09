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
