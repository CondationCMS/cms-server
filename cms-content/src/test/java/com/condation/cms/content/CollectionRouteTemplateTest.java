package com.condation.cms.content;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import java.time.LocalDate;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CollectionRouteTemplateTest {

	@Test
	void rendersMultipleNestedMetadataValues() {
		var template = new CollectionRouteTemplate(detail(
				"/collection/{date}|{location.city}", Map.of()));

		var route = template.render("event-1", Map.of(
				"date", "2026-09-03",
				"location", Map.of("city", "München")));

		Assertions.assertThat(route).isEqualTo("/collection/2026-09-03|muenchen");
	}

	@Test
	void formatsDatesWithJavaDateTimeFormatterNotation() {
		var template = new CollectionRouteTemplate(detail(
				"/events/{date:yyyy}/{date:MM}/{date:dd}", Map.of()));

		Assertions.assertThat(template.render("event-1", Map.of("date", LocalDate.of(2026, 9, 3))))
				.isEqualTo("/events/2026/09/03");
		Assertions.assertThat(template.matches(
				"/events/2026/09/03/", "event-1", Map.of("date", "2026-09-03")))
				.isTrue();
	}

	@Test
	void mapsMetadataValuesBeforeCreatingTheSlug() {
		var template = new CollectionRouteTemplate(detail(
				"/{location.country}/{location.city}",
				Map.of("location.country", Map.of("de", "Germany"))));

		Assertions.assertThat(template.render("event-1", Map.of(
				"location", Map.of("country", "de", "city", "Berlin"))))
				.isEqualTo("/germany/berlin");
	}

	@Test
	void rejectsMissingMappingValues() {
		var template = new CollectionRouteTemplate(detail(
				"/{location.country}",
				Map.of("location.country", Map.of("de", "germany"))));

		Assertions.assertThatIllegalArgumentException()
				.isThrownBy(() -> template.render(
						"event-1", Map.of("location", Map.of("country", "fr"))))
				.withMessageContaining("location.country=fr");
	}

	@Test
	void rejectsSlashesInsideDateFormats() {
		Assertions.assertThatIllegalArgumentException()
				.isThrownBy(() -> detail("/events/{date:yyyy/MM}", Map.of()))
				.withMessageContaining("must not contain '/'");
	}

	private static CollectionDetailConfiguration detail(
			String route,
			Map<String, Map<String, String>> mappings) {
		return new CollectionDetailConfiguration(route, "collections/event.html", mappings);
	}
}
