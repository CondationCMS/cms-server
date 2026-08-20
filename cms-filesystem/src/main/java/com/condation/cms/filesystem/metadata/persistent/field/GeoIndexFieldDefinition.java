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

import java.util.Map;

public record GeoIndexFieldDefinition(String latitude, String longitude)
		implements IndexFieldDefinition {

	public static final String FIELD_TYPE = "geo";

	private static final String DEFAULT_LATITUDE = "latitude";
	private static final String DEFAULT_LONGITUDE = "longitude";

	public GeoIndexFieldDefinition {
		latitude = valueOrDefault(latitude, DEFAULT_LATITUDE);
		longitude = valueOrDefault(longitude, DEFAULT_LONGITUDE);
	}

	@Override
	public String type() {
		return FIELD_TYPE;
	}

	public static GeoIndexFieldDefinition from(Map<?, ?> values) {
		return new GeoIndexFieldDefinition(
				stringValue(values.get(DEFAULT_LATITUDE)),
				stringValue(values.get(DEFAULT_LONGITUDE)));
	}

	private static String stringValue(Object value) {
		return value == null ? null : value.toString();
	}

	private static String valueOrDefault(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value.strip();
	}
}
