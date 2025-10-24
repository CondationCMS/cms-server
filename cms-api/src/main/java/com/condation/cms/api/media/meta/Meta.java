package com.condation.cms.api.media.meta;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class Meta extends HashMap<String, Object> {

	public double getFocalPoint_x() {
		Object value = ((Map<String, Object>) getOrDefault("focal", Collections.emptyMap())).getOrDefault("x", 0.5);
		return toDouble(value, 0.5);
	}

	public double getFocalPoint_y() {
		Object value = ((Map<String, Object>) getOrDefault("focal", Collections.emptyMap())).getOrDefault("y", 0.5);
		return toDouble(value, 0.5);
	}

	private double toDouble(Object value, double defaultValue) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		try {
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

}
