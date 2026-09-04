package com.condation.cms.api.configuration.configs;

/*-
 * #%L
 * CMS Api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Route and template used for collection detail pages.
 */
public record CollectionDetailConfiguration(
		String route,
		String template,
		Map<String, Map<String, String>> mappings) {

	private static final Pattern PARAMETER = Pattern.compile(
			"\\{([a-zA-Z][a-zA-Z0-9_.-]*)(?::([^{}]*))?}");

	public CollectionDetailConfiguration {
		Objects.requireNonNull(route, "collection detail route must not be null");
		Objects.requireNonNull(template, "collection detail template must not be null");

		route = normalizeRoute(route);
		template = template.trim();
		if (template.isEmpty()) {
			throw new IllegalArgumentException("collection detail template must not be blank");
		}

		var copiedMappings = new LinkedHashMap<String, Map<String, String>>();
		Objects.requireNonNull(mappings, "collection detail mappings must not be null")
				.forEach((field, values) -> copiedMappings.put(field, Map.copyOf(values)));
		mappings = Map.copyOf(copiedMappings);

		var parameters = parameters(route);
		if (parameters.isEmpty()) {
			throw new IllegalArgumentException("collection detail route must contain at least one parameter");
		}
		var matcher = PARAMETER.matcher(route);
		var formattedFields = new HashSet<String>();
		while (matcher.find()) {
			var format = matcher.group(2);
			if (format == null) {
				continue;
			}
			if (format.isBlank()) {
				throw new IllegalArgumentException("collection detail date format must not be blank");
			}
			if (format.contains("/")) {
				throw new IllegalArgumentException("collection detail date format must not contain '/': " + format);
			}
			DateTimeFormatter.ofPattern(format);
			formattedFields.add(matcher.group(1));
		}
		for (var field : mappings.keySet()) {
			if (!parameters.contains(field)) {
				throw new IllegalArgumentException("mapping references no route parameter: " + field);
			}
			if (formattedFields.contains(field)) {
				throw new IllegalArgumentException("route parameter cannot have both a format and a mapping: " + field);
			}
		}
	}

	public CollectionDetailConfiguration(String route, String template) {
		this(route, template, Map.of());
	}

	public String parameter() {
		return parameters().getFirst();
	}

	public List<String> parameters() {
		return parameters(route);
	}

	public int parameterOccurrences() {
		return (int) PARAMETER.matcher(route).results().count();
	}

	public boolean hasFormats() {
		return PARAMETER.matcher(route).results().anyMatch(match -> match.group(2) != null);
	}

	private static List<String> parameters(String route) {
		return PARAMETER.matcher(route).results()
				.map(match -> match.group(1))
				.distinct()
				.toList();
	}

	private static String normalizeRoute(String value) {
		var normalized = value.trim();
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		while (normalized.length() > 1 && normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
