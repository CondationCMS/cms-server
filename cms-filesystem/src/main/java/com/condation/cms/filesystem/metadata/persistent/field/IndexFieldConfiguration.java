package com.condation.cms.filesystem.metadata.persistent.field;

/*-
 * #%L
 * CMS FileSystem
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class IndexFieldConfiguration {

	private static final Map<String, Function<Map<?, ?>, IndexFieldDefinition>> DEFINITION_FACTORIES = Map.of(
			GeoIndexFieldDefinition.TYPE, GeoIndexFieldDefinition::from);

	private IndexFieldConfiguration() {
	}

	public static Map<String, IndexFieldDefinition> parse(Map<String, ?> fields) {
		if (fields == null || fields.isEmpty()) {
			return Map.of();
		}

		var result = new LinkedHashMap<String, IndexFieldDefinition>();
		fields.forEach((path, value) -> {
			if (path == null || path.isBlank()) {
				throw new IllegalArgumentException("index field path must not be blank");
			}
			result.put(path.strip(), parseDefinition(value));
		});
		return Map.copyOf(result);
	}

	private static IndexFieldDefinition parseDefinition(Object value) {
		final String type;
		final Map<?, ?> values;
		if (value instanceof String typeName) {
			type = normalizeType(typeName);
			values = Map.of();
		} else if (value instanceof Map<?, ?> definition) {
			type = normalizeType(definition.get("type"));
			values = definition;
		} else {
			throw new IllegalArgumentException("index field definition must be a map or type name");
		}

		var factory = DEFINITION_FACTORIES.get(type);
		if (factory == null) {
			throw new IllegalArgumentException("unsupported index field type: " + type);
		}
		return factory.apply(values);
	}

	private static String normalizeType(Object value) {
		if (value == null || value.toString().isBlank()) {
			throw new IllegalArgumentException("index field type must not be blank");
		}
		return value.toString().strip().toLowerCase(Locale.ROOT);
	}
}
