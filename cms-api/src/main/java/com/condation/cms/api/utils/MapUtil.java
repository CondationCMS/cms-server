package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author t.marx
 */
public class MapUtil {

	public static Object getValue(final Map<String, Object> map, final String field) {
		if (!field.contains(".")) {
			return map.get(field);
		}
		String[] keys = field.split("\\.");
		Map subMap = map;
		for (int i = 0; i < keys.length - 1; i++) {
			subMap = (Map<String, Object>) subMap.getOrDefault(keys[i], Collections.emptyMap());
		}
		return subMap.get(keys[keys.length - 1]);
	}

	public static <T> T getValue(final Map<String, Object> map, final String field, final T defaultValue) {
		if (!field.contains(".")) {
			return (T)map.getOrDefault(field, defaultValue);
		}
		String[] keys = field.split("\\.");
		Map<String, Object> subMap = map;
		for (int i = 0; i < keys.length - 1; i++) {
			subMap = (Map<String, Object>) subMap.getOrDefault(keys[i], Collections.emptyMap());
		}
		return (T) subMap.getOrDefault(keys[keys.length - 1], defaultValue);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void deepMerge(
			Map original,
			Map newMap) {

		for (Map.Entry e : (Set<Map.Entry>) newMap.entrySet()) {
			Object key = e.getKey(),
					value = e.getValue();

			// unfortunately, if null-values are allowed,
			// we suffer the performance hit of double-lookup
			if (original.containsKey(key)) {
				Object originalValue = original.get(key);

				if (Objects.equals(originalValue, value)) {
					continue;
				}

				if (originalValue instanceof Collection) {
					// this could be relaxed to simply to simply add instead of addAll
					// IF it's not a collection (still addAll if it is),
					// this would be a useful approach, but uncomfortably inconsistent, algebraically
					Preconditions.checkArgument(value instanceof Collection,
							"a non-collection collided with a collection: %s%n\t%s",
							value, originalValue);

					((Collection) originalValue).addAll((Collection) value);

					continue;
				}

				if (originalValue instanceof Map) {
					Preconditions.checkArgument(value instanceof Map,
							"a non-map collided with a map: %s%n\t%s",
							value, originalValue);

					deepMerge((Map) originalValue, (Map) value);

					continue;
				}

				original.put(key, value);

			} else {
				original.put(key, value);
			}
		}
	}
}
