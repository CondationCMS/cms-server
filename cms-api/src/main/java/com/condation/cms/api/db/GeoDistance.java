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

import java.util.Objects;

public final class GeoDistance {

	private static final double EARTH_RADIUS_METERS = 6_371_008.7714;

	private GeoDistance() {
	}

	public static double between(
			double originLatitude,
			double originLongitude,
			double latitude,
			double longitude,
			DistanceUnit unit) {
		Objects.requireNonNull(unit, "distance unit must not be null");
		validateCoordinate(originLatitude, originLongitude);
		validateCoordinate(latitude, longitude);

		double latitudeDelta = Math.toRadians(latitude - originLatitude);
		double longitudeDelta = Math.toRadians(longitude - originLongitude);
		double originLatitudeRadians = Math.toRadians(originLatitude);
		double latitudeRadians = Math.toRadians(latitude);
		double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
				+ Math.cos(originLatitudeRadians)
				* Math.cos(latitudeRadians)
				* Math.pow(Math.sin(longitudeDelta / 2), 2);
		haversine = Math.min(1, haversine);
		double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
		return unit.fromMeters(EARTH_RADIUS_METERS * centralAngle);
	}

	public static double between(
			double originLatitude,
			double originLongitude,
			double latitude,
			double longitude,
			String unit) {
		return between(
				originLatitude,
				originLongitude,
				latitude,
				longitude,
				DistanceUnit.fromString(unit));
	}

	private static void validateCoordinate(double latitude, double longitude) {
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
			throw new IllegalArgumentException("latitude must be between -90 and 90");
		}
		if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
			throw new IllegalArgumentException("longitude must be between -180 and 180");
		}
	}
}
