package com.condation.cms.core.configuration;

/*-
 * #%L
 * CMS Core
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.core.configuration.configs.CollectionConfiguration;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CollectionConfigurationTest {

	@Test
	void updatesTheSharedConfigurationOnReload() {
		var eventBus = mock(EventBus.class);
		var source = mock(ConfigSource.class);
		var initial = Map.<String, Object>of(
				"blog",
				Map.of("site", "content-site", "detail", Map.of(
						"route", "/blog/{slug}",
						"template", "collections/blog.html")),
				"listing-only",
				Map.of());
		var updated = Map.<String, Object>of(
				"products",
				Map.of("detail", Map.of(
						"route", "/products/{id}",
						"template", "collections/product.html")));

		when(source.exists()).thenReturn(true);
		when(source.reload()).thenReturn(false, true);
		when(source.getMap("collections")).thenReturn(initial, updated);

		var configuration = CollectionConfiguration.builder(eventBus)
				.id("collections")
				.addSource(source)
				.build();
		var apiConfiguration = configuration.apiConfiguration();
		var initialSnapshot = configuration.getCollections();

		Assertions.assertThat(initialSnapshot).containsOnlyKeys("blog", "listing-only");
		Assertions.assertThat(initialSnapshot.get("blog").detailPage().orElseThrow().parameter())
				.isEqualTo("slug");
		Assertions.assertThat(initialSnapshot.get("blog").sourceSite()).contains("content-site");
		Assertions.assertThat(initialSnapshot.get("listing-only").sourceSite()).isEmpty();

		configuration.reload();

		var updatedSnapshot = configuration.getCollections();
		Assertions.assertThat(configuration.apiConfiguration()).isSameAs(apiConfiguration);
		Assertions.assertThat(apiConfiguration.collections()).isSameAs(updatedSnapshot);
		Assertions.assertThat(updatedSnapshot).isNotSameAs(initialSnapshot);
		Assertions.assertThat(initialSnapshot).containsOnlyKeys("blog", "listing-only");
		Assertions.assertThat(updatedSnapshot).containsOnlyKeys("products");
		Assertions.assertThat(updatedSnapshot.get("products").detailPage().orElseThrow().route())
				.isEqualTo("/products/{id}");
		verify(eventBus).publish(new ConfigurationReloadEvent("collections"));
	}

	@Test
	void keepsTheCompletePreviousSnapshotWhenOneDefinitionIsInvalid() {
		var eventBus = mock(EventBus.class);
		var source = mock(ConfigSource.class);
		var initial = Map.<String, Object>of(
				"blog", Map.of("detail", Map.of(
						"route", "/blog/{id}",
						"template", "collections/blog.html")));
		var invalid = Map.<String, Object>of(
				"products", Map.of("detail", Map.of(
						"route", "/products/{id}",
						"template", "collections/product.html")),
				"broken", Map.of("detail", Map.of("route", "/broken/{id}")));
		when(source.exists()).thenReturn(true);
		when(source.reload()).thenReturn(false, true);
		when(source.getMap("collections")).thenReturn(initial, invalid);

		var configuration = CollectionConfiguration.builder(eventBus)
				.id("collections")
				.addSource(source)
				.build();
		var initialSnapshot = configuration.getCollections();

		configuration.reload();

		Assertions.assertThat(configuration.getCollections()).isSameAs(initialSnapshot);
		Assertions.assertThat(configuration.getCollections()).containsOnlyKeys("blog");
		verifyNoInteractions(eventBus);
	}

	@Test
	void readsRouteFormatsAndMappings() {
		var source = mock(ConfigSource.class);
		when(source.exists()).thenReturn(true);
		when(source.getMap("collections")).thenReturn(Map.of(
				"events", Map.of("detail", Map.of(
						"route", "/events/{date:yyyy}/{date:MM}/{date:dd}/{location.country}/{location.city}",
						"template", "collections/event.html",
						"mappings", Map.of("location.country", Map.of("de", "germany"))))));

		var detail = CollectionConfiguration.builder(null)
				.addSource(source)
				.build()
				.getCollections()
				.get("events")
				.detailPage()
				.orElseThrow();

		Assertions.assertThat(detail.parameters())
				.containsExactly("date", "location.country", "location.city");
		Assertions.assertThat(detail.parameterOccurrences()).isEqualTo(5);
		Assertions.assertThat(detail.hasFormats()).isTrue();
		Assertions.assertThat(detail.mappings().get("location.country"))
				.containsEntry("de", "germany");
	}
}
