package com.condation.cms.content.template.functions.geo;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.GeoDistance;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class GeoFunction {

	public double distance(
			Object originLatitude,
			Object originLongitude,
			Object latitude,
			Object longitude,
			String unit) {
		return GeoDistance.between(
				number(originLatitude, "origin latitude"),
				number(originLongitude, "origin longitude"),
				number(latitude, "latitude"),
				number(longitude, "longitude"),
				unit);
	}

	public double distance(
			Object originLatitude,
			Object originLongitude,
			Object latitude,
			Object longitude,
			String unit,
			Object decimals) {
		double value = distance(originLatitude, originLongitude, latitude, longitude, unit);
		int scale = integer(decimals, "decimals");
		if (scale < 0 || scale > 10) {
			throw new IllegalArgumentException("decimals must be between 0 and 10");
		}
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	private static double number(Object value, String name) {
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		if (value instanceof String string) {
			try {
				return Double.parseDouble(string);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(name + " must be numeric", ex);
			}
		}
		throw new IllegalArgumentException(name + " must be numeric");
	}

	private static int integer(Object value, String name) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String string) {
			try {
				return Integer.parseInt(string);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(name + " must be an integer", ex);
			}
		}
		throw new IllegalArgumentException(name + " must be an integer");
	}
}
