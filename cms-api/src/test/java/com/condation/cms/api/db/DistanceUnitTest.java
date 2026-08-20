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

class DistanceUnitTest {

	@Test
	void parsesTemplateFriendlyUnitNames() {
		Assertions.assertThat(DistanceUnit.fromString("m")).isEqualTo(DistanceUnit.METERS);
		Assertions.assertThat(DistanceUnit.fromString(" meters ")).isEqualTo(DistanceUnit.METERS);
		Assertions.assertThat(DistanceUnit.fromString("KM")).isEqualTo(DistanceUnit.KILOMETERS);
		Assertions.assertThat(DistanceUnit.fromString("kilometers")).isEqualTo(DistanceUnit.KILOMETERS);
		Assertions.assertThat(DistanceUnit.fromString("mi")).isEqualTo(DistanceUnit.MILES);
		Assertions.assertThat(DistanceUnit.fromString(" MILES ")).isEqualTo(DistanceUnit.MILES);
	}

	@Test
	void rejectsUnknownUnitNames() {
		Assertions.assertThatThrownBy(() -> DistanceUnit.fromString("yards"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("yards");
	}

	@Test
	void convertsValuesToMeters() {
		Assertions.assertThat(DistanceUnit.METERS.toMeters(2.5)).isEqualTo(2.5);
		Assertions.assertThat(DistanceUnit.KILOMETERS.toMeters(2.5)).isEqualTo(2_500);
		Assertions.assertThat(DistanceUnit.MILES.toMeters(2.5)).isEqualTo(4_023.36);
		Assertions.assertThat(DistanceUnit.MILES.fromMeters(1_609.344)).isEqualTo(1);
	}
}
