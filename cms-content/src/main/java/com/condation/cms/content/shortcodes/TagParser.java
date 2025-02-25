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
import com.condation.cms.api.request.RequestContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.*;
import java.util.function.Function;

public class TagParser {

	private final JexlEngine engine;

	public TagParser(JexlEngine engine) {
		this.engine = engine;
	}

	// Klasse zur Speicherung der Tag-Informationen
	public static record TagInfo(String name, Parameter rawAttributes, int startIndex, int endIndex) {

	}

	// Erster Schritt: Alle Tags ermitteln und deren Positionen sowie Roh-Attribute speichern
	public List<TagInfo> findTags(String text, TagMap tagHandlers) {
		List<TagInfo> tags = new ArrayList<>();
		int i = 0;

		while (i < text.length()) {
			if (text.charAt(i) == '[' && i + 1 < text.length() && text.charAt(i + 1) == '[') {
				int tagStart = i;
				int endTagIndex = findTagEnd(text, i);
				if (endTagIndex != -1) {
					String tagContent = text.substring(i + 2, endTagIndex).trim();
					boolean isSelfClosing = tagContent.endsWith("/");

					if (isSelfClosing) {
						tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
					}

					int spaceIndex = tagContent.indexOf(' ');
					String tagName = spaceIndex == -1 ? tagContent : tagContent.substring(0, spaceIndex);
					Parameter rawAttributes = spaceIndex == -1
							? new Parameter()
							: parseRawAttributes(tagContent.substring(spaceIndex + 1));

					int closingTagIndex = -1;
					if (!isSelfClosing) {
						closingTagIndex = text.indexOf("[[/" + tagName + "]]", endTagIndex + 2);
						if (closingTagIndex != -1) {
							String content = text.substring(endTagIndex + 2, closingTagIndex);
							rawAttributes.put("_content", content);
							endTagIndex = closingTagIndex + ("[[/" + tagName + "]]").length() - 2;
						}
					}

					if (tagHandlers.has(tagName)) {
						tags.add(new TagInfo(tagName, rawAttributes, tagStart, endTagIndex + 2));
						i = endTagIndex + 2; // Zum nächsten Tag springen
					} else {
						i++;
					}
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
		return tags;
	}

	public String parse(String text, TagMap tagHandlers, RequestContext requestContext) {
		return parse(text, tagHandlers, Collections.emptyMap(), requestContext);
	}

	// Zweiter Schritt: Tags basierend auf den gespeicherten Positionen ersetzen
	public String parse(String text, TagMap tagHandlers, Map<String, Object> contextModel, RequestContext requestContext) {
		// Erster Schritt: Finde alle Tags
		List<TagInfo> tags = findTags(text, tagHandlers);

		// Zweiter Schritt: Ersetze alle Tags im Text
		StringBuilder result = new StringBuilder();
		int lastIndex = 0;
		for (TagInfo tag : tags) {
			result.append(text, lastIndex, tag.startIndex); // Unveränderten Teil des Textes hinzufügen
			Function<Parameter, String> handler = tagHandlers.get(tag.name);

			// Im zweiten Schritt: Attribute auswerten
			Parameter evaluatedAttributes = evaluateAttributes(tag.rawAttributes, contextModel, requestContext);

			if (evaluatedAttributes.containsKey("_content")) {
				String rawContent = (String) evaluatedAttributes.get("_content");
				String parsedContent = parse(rawContent, tagHandlers, contextModel, requestContext); // Rekursives Parsen
				evaluatedAttributes.put("_content", parsedContent);
			}

			result.append(handler.apply(evaluatedAttributes)); // Tag-Ersetzung
			lastIndex = tag.endIndex; // Aktualisiere den Startpunkt für den nächsten Tag
		}
		result.append(text.substring(lastIndex)); // Füge den restlichen Text hinzu

		return result.toString();
	}

	// Methode zum Finden des Endes eines Tags
	private int findTagEnd(String text, int startIndex) {
		for (int i = startIndex; i < text.length() - 1; i++) {
			if (text.charAt(i) == ']' && text.charAt(i + 1) == ']') {
				return i;
			}
		}
		return -1; // Kein schließendes ']]' gefunden
	}

	// Methode zur Attribut-Analyse im ersten Schritt (Rohwerte als Strings speichern)
	private Parameter parseRawAttributes(String attributesString) {
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
					attributes.put(key.toString().trim(), value.toString().trim()); // Rohwert speichern
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
			attributes.put(key.toString().trim(), value.toString().trim()); // Rohwert speichern
		}

		return attributes;
	}

	// Zweiter Schritt: Attribute auswerten
	private Parameter evaluateAttributes(Parameter rawAttributes, Map<String, Object> contextModel, RequestContext requestContext) {
		Parameter evaluatedAttributes = new Parameter(requestContext);
		for (Map.Entry<String, Object> entry : rawAttributes.entrySet()) {
			String key = entry.getKey();
			String rawValue = (String) entry.getValue(); // Rohwert als String
			evaluatedAttributes.put(key, parseValue(rawValue, contextModel, requestContext)); // Wert erst jetzt parsen
		}
		return evaluatedAttributes;
	}

	// Methode zur Auswertung von Attributwerten im zweiten Schritt
	private Object parseValue(String value, Map<String, Object> contextModel, RequestContext requestContext) {
		if (value.matches("\\d+")) {
			return Integer.valueOf(value);
		} else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return Boolean.valueOf(value);
		} else if (value.startsWith("${") && value.endsWith("}")) {
			String expressionString = value.substring(2, value.length() - 1);

			var contextMap = new HashMap<String, Object>();
			contextMap.putAll(contextModel);
			if (requestContext != null) {
				contextMap.putAll(requestContext.getVariables());			
			}
			
			var expression = engine.createExpression(expressionString);
			return expression.evaluate(new MapContext(contextMap));
		}
		return value;
	}
}
