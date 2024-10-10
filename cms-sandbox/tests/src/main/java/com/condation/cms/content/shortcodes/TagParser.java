package com.condation.cms.content.shortcodes;

import java.util.*;
import java.util.function.Function;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

public class TagParser {

	private final Codes tagHandlers;
	private final JexlEngine engine;

	public TagParser(Codes tagHandlers, JexlEngine engine) {
		this.tagHandlers = tagHandlers;
		this.engine = engine;
	}

	public String parse(String text) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			if (text.charAt(i) == '[' && i + 1 < text.length() && text.charAt(i + 1) == '[') {
				int tagStart = i;
				i = parseTag(text, i, result);
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

	private int parseTag(String text, int index, StringBuilder result) {
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
		Map<String, Object> attributes = spaceIndex == -1 ? new HashMap<>() : parseAttributes(tagContent.substring(spaceIndex + 1));

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

		if (tagHandlers.hasCode(tagName)) {
			Function<Map<String, Object>, String> handler = tagHandlers.get(tagName);
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

	private Map<String, Object> parseAttributes(String attributesString) {
		Map<String, Object> attributes = new HashMap<>();
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
					attributes.put(key.toString().trim(), parseValue(value.toString().trim()));
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
			attributes.put(key.toString().trim(), parseValue(value.toString().trim()));
		}

		return attributes;
	}

	private Object parseValue(String value) {
		if (value.matches("\\d+")) {
			return Integer.valueOf(value);
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return Boolean.valueOf(value);
		} else if (value.startsWith("${") && value.endsWith("}")) {
			String expressionString = value.substring(2, value.length() - 1);
			
			var expression = engine.createExpression(expressionString);
			return expression.evaluate(new MapContext());
		}
		return value;
	}

	public static class Codes {

		private final Map<String, Function<Map<String, Object>, String>> codes = new HashMap<>();

		public void add(String codeName, Function<Map<String, Object>, String> function) {
			codes.put(codeName, function);
		}

		public boolean hasCode(String codeName) {
			return codes.containsKey(codeName);
		}

		public Function<Map<String, Object>, String> get(String codeName) {
			return codes.getOrDefault(codeName, (params) -> "");
		}
	}
}
