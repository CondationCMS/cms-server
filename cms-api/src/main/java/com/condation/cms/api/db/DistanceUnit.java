package com.condation.cms.api.db;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Locale;

/**
 * Distance units accepted by geo queries.
 *
 * @author t.marx
 */
public enum DistanceUnit {

	METERS(1),
	KILOMETERS(1_000),
	MILES(1_609.344);

	private final double meters;

	DistanceUnit(double meters) {
		this.meters = meters;
	}

	public double toMeters(double value) {
		return value * meters;
	}

	public double fromMeters(double value) {
		return value / meters;
	}

	public static DistanceUnit fromString(String value) {
		if (value == null) {
			throw new IllegalArgumentException("distance unit must not be null");
		}

		return switch (value.strip().toLowerCase(Locale.ROOT)) {
			case "m", "meter", "meters" -> METERS;
			case "km", "kilometer", "kilometers" -> KILOMETERS;
			case "mi", "mile", "miles" -> MILES;
			default -> throw new IllegalArgumentException("unsupported distance unit: " + value);
		};
	}
}
