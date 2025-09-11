package com.condation.cms.templates.utils;

/*-
 * #%L
 * templates
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.condation.cms.templates.parser.Filter;

/**
 *
 * @author t.marx
 */
public class TemplateUtils {

	public static final String CHECK_FILTER_REGEX = "\\s*\\w+\\s*\\|";
	public static final Pattern CHECK_FILTER_PATTERN = Pattern.compile(CHECK_FILTER_REGEX);

	public static final String GET_FILTER_REGEX = "\\s*(.*?)\\s*";
	public static final Pattern GET_FILTER_PATTERN = Pattern.compile(GET_FILTER_REGEX);

	public static boolean hasFilters(String expression) {
		if (expression == null || expression.isBlank()) {
			return false;
		}

		String[] parts = expression.split("\\s+\\|\\s+"); // Nur " | " als Trenner verwenden
		return parts.length > 1;
	}

	public static List<String> extractFilters(String expression) {
		List<String> filters = new ArrayList<>();
		if (expression == null || expression.isBlank()) {
			return filters;
		}

		String[] parts = expression.split("\\s+\\|\\s+");
		if (parts.length < 2) {
			return filters;
		}

		for (int i = 1; i < parts.length; i++) {
			filters.add(parts[i]);
		}

		return filters;
	}

	public static Filter parseFilter(String filterDefinition) {
		// Regex nur für Filtername und Gesamte Parameter
		String regex = "^(\\w+)\\s*(?:\\((.*)\\))?$";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(filterDefinition.trim());

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid filter definition: " + filterDefinition);
		}

		String filterName = matcher.group(1);
		String paramsString = matcher.group(2);

		List<String> parameters = parseParameters(paramsString);
		return new Filter(filterName, parameters);
	}

	/**
	 * Parst eine Parameterliste, unterstützt Kommas innerhalb von Hochkommas.
	 */
	private static List<String> parseParameters(String paramsString) {
		List<String> parameters = new ArrayList<>();
		if (paramsString == null || paramsString.isBlank()) {
			return parameters;
		}

		StringBuilder current = new StringBuilder();
		boolean inSingleQuotes = false;
		boolean inDoubleQuotes = false;

		for (int i = 0; i < paramsString.length(); i++) {
			char c = paramsString.charAt(i);

			if (c == '\'' && !inDoubleQuotes) {
				inSingleQuotes = !inSingleQuotes;
				current.append(c);
			} else if (c == '"' && !inSingleQuotes) {
				inDoubleQuotes = !inDoubleQuotes;
				current.append(c);
			} else if (c == ',' && !inSingleQuotes && !inDoubleQuotes) {
				// Parameterende
				parameters.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}

		if (current.length() > 0) {
			parameters.add(current.toString().trim());
		}

		return parameters;
	}

	public static String extractVariableName(String input) {
		// Split basierend auf "|"
		String[] parts = input.split("\\|");

		// Rückgabe des ersten Elements (Variablenname), getrimmt
		return parts[0].trim();
	}
}
