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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IndexFieldConfigurationTest {

	@Test
	void createsTheDefinitionClassRegisteredForTheConfiguredType() {
		var definitions = IndexFieldConfiguration.parse(Map.of(
				"address.location",
				Map.of("type", "geo", "latitude", "lat", "longitude", "lon")));

		Assertions.assertThat(definitions.get("address.location"))
				.isInstanceOfSatisfying(GeoIndexFieldDefinition.class, definition -> {
					Assertions.assertThat(definition.type()).isEqualTo("geo");
					Assertions.assertThat(definition.latitude()).isEqualTo("lat");
					Assertions.assertThat(definition.longitude()).isEqualTo("lon");
				});
	}

	@Test
	void usesTypeSpecificDefaultsForTheCompactConfiguration() {
		var definitions = IndexFieldConfiguration.parse(Map.of("location", "geo"));

		Assertions.assertThat(definitions.get("location"))
				.isEqualTo(new GeoIndexFieldDefinition("latitude", "longitude"));
	}

	@Test
	void rejectsTypesWithoutARegisteredDefinitionFactory() {
		Assertions.assertThatThrownBy(() -> IndexFieldConfiguration.parse(Map.of("price", "money")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("money");
	}
}
