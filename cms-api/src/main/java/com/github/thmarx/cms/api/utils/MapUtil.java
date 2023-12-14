package com.github.thmarx.cms.api.utils;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 Marx-Software
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

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class MapUtil {
	public static Object getValue(final Map<String, Object> map, final String field) {
		String[] keys = field.split("\\.");
		Map subMap = map;
		for (int i = 0; i < keys.length - 1; i++) {
			subMap = (Map<String, Object>) subMap.getOrDefault(keys[i], Collections.emptyMap());
		}
		return subMap.get(keys[keys.length - 1]);
	}

	public static <T> T getValue(final Map<String, Object> map, final String field, final T defaultValue) {
		String[] keys = field.split("\\.");
		Map<String, Object> subMap = map;
		for (int i = 0; i < keys.length - 1; i++) {
			subMap = (Map<String, Object>) subMap.getOrDefault(keys[i], Collections.emptyMap());
		}
		return (T) subMap.getOrDefault(keys[keys.length - 1], defaultValue);
	}
}
