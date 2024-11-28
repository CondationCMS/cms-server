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

	public static boolean hasFilters(String variable) {
		Matcher matcher = CHECK_FILTER_PATTERN.matcher(variable);
		return matcher.find();
	}

	public static List<String> extractFilters(String variable) {
		List<String> filters = new ArrayList<>();

		// Split basierend auf "|"
		String[] parts = variable.split("\\|");

		// Entferne den Variablennamen (erstes Element) und trimme Filter
		for (int i = 1; i < parts.length; i++) {
			filters.add(parts[i].trim());
		}

		return filters;
	}

	public static Filter parseFilter(String filterDefinition) {
        // Regular expression to match the filter name and parameters
        String regex = "^(\\w+)(?:\\((.*?)\\))?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(filterDefinition);

        if (matcher.matches()) {
            String filterName = matcher.group(1); // Filter name
            String paramsString = matcher.group(2); // Optional parameters

            // Split the parameters if present, otherwise return an empty list
            List<String> parameters = new ArrayList<>();
            if (paramsString != null && !paramsString.isBlank()) {
                for (String param : paramsString.split(",")) {
                    parameters.add(param.trim());
                }
            }

            return new Filter(filterName, parameters);
        } else {
            throw new IllegalArgumentException("Invalid filter definition: " + filterDefinition);
        }
    }

	public static String extractVariableName(String input) {
		// Split basierend auf "|"
		String[] parts = input.split("\\|");

		// Rückgabe des ersten Elements (Variablenname), getrimmt
		return parts[0].trim();
	}
}
