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
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

public class TagParser {

	private final JexlEngine engine;

	public TagParser(JexlEngine engine) {
		this.engine = engine;
	}

	public String parse(String text, TagMap tagHandlers) {
		return parse(text, tagHandlers, Collections.emptyMap());
	}
	
	public String parse(String text, TagMap tagHandlers, Map<String, Object> contextModel) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			if (text.charAt(i) == '[' && i + 1 < text.length() && text.charAt(i + 1) == '[') {
				int tagStart = i;
				i = parseTag(text, i, result, tagHandlers, contextModel);
				if (i == tagStart) { // Kein gültiger Tag gefunden, füge '[[' hinzu.
					result.append("[[");
					i += 2;
				}
			} else {
				result.append(text.charAt(i));
				i++;
			}
		}
		return result.toString();
	}

	private int parseTag(String text, int index, StringBuilder result, TagMap tagHandlers, Map<String, Object> contextModel) {
		int endTagIndex = findTagEnd(text, index);
		if (endTagIndex == -1) {
			return index; // Kein schließendes ']]' gefunden
		}

		String tagContent = text.substring(index + 2, endTagIndex).trim();
		boolean isSelfClosing = tagContent.endsWith("/");

		if (isSelfClosing) {
			tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
		}

		int spaceIndex = tagContent.indexOf(' ');
		String tagName = spaceIndex == -1 ? tagContent : tagContent.substring(0, spaceIndex);
		Parameter attributes = spaceIndex == -1 
				? new Parameter() 
				: parseAttributes(tagContent.substring(spaceIndex + 1), contextModel);

		int closingTagIndex = -1;
		if (!isSelfClosing) {
			closingTagIndex = text.indexOf("[[/" + tagName + "]]", endTagIndex + 2);
			if (closingTagIndex != -1) {
				// Verarbeite den Content für geöffnete und geschlossene Tags
				String content = text.substring(endTagIndex + 2, closingTagIndex);
				attributes.put("_content", content);
				endTagIndex = closingTagIndex + ("[[/" + tagName + "]]").length() - 2;
			}
		}

		if (tagHandlers.has(tagName)) {
			Function<Parameter, String> handler = tagHandlers.get(tagName);
			result.append(handler.apply(attributes));
			// Setze den Index auf das Zeichen direkt nach dem schließenden Tag oder schließenden Tag mit Content
			return endTagIndex + 2;
		}

		return index; // Tag nicht erkannt
	}

	private int findTagEnd(String text, int startIndex) {
		for (int i = startIndex; i < text.length() - 1; i++) {
			if (text.charAt(i) == ']' && text.charAt(i + 1) == ']') {
				return i;
			}
		}
		return -1; // Kein schließendes ']]' gefunden
	}

	private Parameter parseAttributes(String attributesString, Map<String, Object> contextModel) {
		Parameter attributes = new Parameter();
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean inQuotes = false;
		boolean readingKey = true;

		for (int i = 0; i < attributesString.length(); i++) {
			char c = attributesString.charAt(i);
			if (c == '"' || c == '\'') {
				inQuotes = !inQuotes;
			} else if (!inQuotes && (c == '=' || c == ' ')) {
				if (readingKey) {
					readingKey = false;
				} else {
					attributes.put(key.toString().trim(), parseValue(value.toString().trim(), contextModel));
					key.setLength(0);
					value.setLength(0);
					readingKey = true;
				}
			} else {
				if (readingKey) {
					key.append(c);
				} else {
					value.append(c);
				}
			}
		}

		// Letztes Attribut verarbeiten
		if (key.length() > 0 && value.length() > 0) {
			attributes.put(key.toString().trim(), parseValue(value.toString().trim(), contextModel));
		}

		return attributes;
	}

	private Object parseValue(String value, Map<String, Object> contextModel) {
		if (value.matches("\\d+")) {
			return Integer.valueOf(value);
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return Boolean.valueOf(value);
		} else if (value.startsWith("${") && value.endsWith("}")) {
			String expressionString = value.substring(2, value.length() - 1);
			
			var expression = engine.createExpression(expressionString);
			return expression.evaluate(new MapContext(contextModel));
		}
		return value;
	}
}
