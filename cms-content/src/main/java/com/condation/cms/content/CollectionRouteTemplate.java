package com.condation.cms.content;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.content.utils.SlugUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/** Renders and matches the small template language used by collection routes. */
public final class CollectionRouteTemplate {

	private static final Pattern TOKEN = Pattern.compile(
			"\\{([a-zA-Z][a-zA-Z0-9_.-]*)(?::([^{}]*))?}");

	private final CollectionDetailConfiguration configuration;
	private final Pattern shape;

	public CollectionRouteTemplate(CollectionDetailConfiguration configuration) {
		this.configuration = configuration;
		this.shape = compileShape(configuration.route());
	}

	public String render(String id, Map<String, Object> metadata) {
		var route = new StringBuilder();
		var matcher = TOKEN.matcher(configuration.route());
		var end = 0;
		while (matcher.find()) {
			route.append(configuration.route(), end, matcher.start());
			var field = matcher.group(1);
			var value = "id".equals(field) ? id : MapUtil.getValue(metadata, field);
			if (value == null || value.toString().isBlank()) {
				throw new IllegalArgumentException("collection item has no route value for: " + field);
			}
			route.append(toRouteValue(field, matcher.group(2), value));
			end = matcher.end();
		}
		route.append(configuration.route(), end, configuration.route().length());
		return route.toString();
	}

	public boolean matchesShape(String uri) {
		return shape.matcher(normalizeUri(uri)).matches();
	}

	public boolean matches(String uri, String id, Map<String, Object> metadata) {
		try {
			return normalizeUri(uri).equals(normalizeUri(render(id, metadata)));
		} catch (IllegalArgumentException _) {
			return false;
		}
	}

	private String toRouteValue(String field, String format, Object rawValue) {
		if ("id".equals(field)
				&& !configuration.mappings().containsKey(field)
				&& format == null) {
			return rawValue.toString();
		}

		var mapping = configuration.mappings().get(field);
		if (mapping != null) {
			var mapped = mapping.get(rawValue.toString());
			if (mapped == null) {
				throw new IllegalArgumentException(
						"collection item route value has no mapping for: " + field + "=" + rawValue);
			}
			return slugifyPath(mapped, field);
		}

		if (format != null) {
			return slugifyPath(formatDate(rawValue, format, field), field);
		}
		return slugifyPath(rawValue.toString(), field);
	}

	private static String formatDate(Object value, String pattern, String field) {
		var formatter = DateTimeFormatter.ofPattern(pattern);
		TemporalAccessor temporal = switch (value) {
			case Date date -> date.toInstant().atZone(ZoneId.systemDefault());
			case TemporalAccessor accessor -> accessor;
			case CharSequence text -> parseIsoDate(text.toString(), field);
			default -> throw new IllegalArgumentException(
					"collection item route value is not a date: " + field);
		};
		try {
			return formatter.format(temporal);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException("collection item route date cannot be formatted: " + field, ex);
		}
	}

	private static TemporalAccessor parseIsoDate(String value, String field) {
		for (var parser : java.util.List.<java.util.function.Function<String, TemporalAccessor>>of(
				ZonedDateTime::parse,
				OffsetDateTime::parse,
				LocalDateTime::parse,
				LocalDate::parse,
				text -> Instant.parse(text).atZone(ZoneId.systemDefault()))) {
			try {
				return parser.apply(value);
			} catch (DateTimeParseException _) {
				// Try the next ISO-8601 representation.
			}
		}
		throw new IllegalArgumentException("collection item route value is not an ISO date: " + field);
	}

	private static String slugifyPath(String value, String field) {
		var segments = value.split("/", -1);
		var result = new StringBuilder();
		for (var index = 0; index < segments.length; index++) {
			var segment = SlugUtil.slugify(segments[index]);
			if (segment.isBlank()) {
				throw new IllegalArgumentException(
						"collection item route value cannot be converted to a slug: " + field);
			}
			if (index > 0) {
				result.append('/');
			}
			result.append(segment);
		}
		return result.toString();
	}

	private static Pattern compileShape(String route) {
		var regex = new StringBuilder("^");
		var matcher = TOKEN.matcher(route);
		var end = 0;
		while (matcher.find()) {
			regex.append(Pattern.quote(route.substring(end, matcher.start()))).append(".+?");
			end = matcher.end();
		}
		regex.append(Pattern.quote(route.substring(end))).append("/?$");
		return Pattern.compile(regex.toString());
	}

	static String normalizeUri(String uri) {
		var normalized = uri == null ? "" : uri.trim();
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		while (normalized.length() > 1 && normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
