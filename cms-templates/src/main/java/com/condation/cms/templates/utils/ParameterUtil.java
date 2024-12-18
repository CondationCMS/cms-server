package com.condation.cms.templates.utils;

/*-
 * #%L
 * cms-templates
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

import com.condation.cms.templates.renderer.Renderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

/**
 *
 * @author t.marx
 */
public class ParameterUtil {
	public static Map<String, Object> parseAndEvaluate(String input, JexlContext jexlContext, JexlEngine engine) {
		Map<String, Object> resultMap = new HashMap<>();

		// Tokenize den Eingabestring (Leerzeichen als Trennung der Parameter)
		List<String> tokens = tokenize(input);

		for (String token : tokens) {
			int equalsIndex = token.indexOf('=');
			if (equalsIndex > 0) {
				String key = token.substring(0, equalsIndex).trim(); // Schlüssel extrahieren
				String value = token.substring(equalsIndex + 1).trim(); // Wert extrahieren

				// Anführungszeichen entfernen, falls vorhanden
				if (value.startsWith("\"") && value.endsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}

				// Wert mit JEXL evaluieren
				Object evaluatedValue;
				try {
					JexlExpression expression = engine.createExpression(value);
					evaluatedValue = expression.evaluate(jexlContext);
				} catch (Exception e) {
					// Falls der Wert keine JEXL-Expression ist, einfach als String speichern
					evaluatedValue = value;
				}

				resultMap.put(key, evaluatedValue);
			}
		}

		return resultMap;
	}

	private static List<String> tokenize(String input) {
		List<String> tokens = new ArrayList<>();
		StringBuilder currentToken = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);

			if (c == '"') {
				inQuotes = !inQuotes; // Zustand der Anführungszeichen umkehren
				currentToken.append(c);
			} else if (c == ' ' && !inQuotes) {
				// Bei Leerzeichen trennen, sofern nicht in Anführungszeichen
				if (currentToken.length() > 0) {
					tokens.add(currentToken.toString());
					currentToken.setLength(0);
				}
			} else {
				currentToken.append(c);
			}
		}

		if (currentToken.length() > 0) {
			tokens.add(currentToken.toString());
		}

		return tokens;
	}
}
