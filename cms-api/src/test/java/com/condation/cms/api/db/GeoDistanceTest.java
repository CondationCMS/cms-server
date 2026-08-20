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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class GeoDistanceTest {

	@Test
	void calculatesDistanceInTheRequestedUnit() {
		double kilometers = GeoDistance.between(51.4818, 7.2162, 51.4882, 7.2160, "km");
		double miles = GeoDistance.between(51.4818, 7.2162, 51.4882, 7.2160, "miles");

		Assertions.assertThat(kilometers).isBetween(0.70, 0.72);
		Assertions.assertThat(miles).isBetween(0.43, 0.45);
	}

	@Test
	void rejectsInvalidCoordinates() {
		Assertions.assertThatThrownBy(() -> GeoDistance.between(91, 7.2162, 51.4882, 7.2160, "km"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("latitude");
	}
}
