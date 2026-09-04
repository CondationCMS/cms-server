package com.condation.cms.content.template.functions;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import java.util.Map;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkFunctionTest {

	private final ConcurrentHashMap<String, CollectionDefinition> definitions = new ConcurrentHashMap<>();
	private final RequestContext context = new RequestContext();
	private CollectionConfiguration collectionConfiguration;
	private final CollectionItem item = new CollectionItem(
			"item_1",
			"blog",
			"blog/item_1.md",
			"",
			Map.of("slug", "first-post"));

	@BeforeEach
	void setUp() {
		var configuration = new Configuration();
		collectionConfiguration = new CollectionConfiguration(definitions);
		configuration.add(CollectionConfiguration.class, collectionConfiguration);
		context.add(ConfigurationFeature.class, new ConfigurationFeature(configuration));

		var siteProperties = mock(SiteProperties.class);
		when(siteProperties.contextPath()).thenReturn("/docs");
		context.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
	}

	@Test
	void createsContextAwareUrlUsingTheItemId() {
		define("/articles/{id}");

		var url = new LinkFunction(context).collectionUrl(item);

		Assertions.assertThat(url).isEqualTo("/docs/articles/item_1");
	}

	@Test
	void createsContextAwareUrlUsingConfiguredFrontMatter() {
		define("/articles/{slug}");

		var url = new LinkFunction(context).collectionUrl(item);

		Assertions.assertThat(url).isEqualTo("/docs/articles/first-post");
	}

	@Test
	void slugifiesConfiguredFrontMatterForTheUrl() {
		define("/articles/{slug}");
		var itemWithUnnormalizedSlug = new CollectionItem(
				"item_2",
				"blog",
				"blog/item_2.md",
				"",
				Map.of("slug", "Über uns & das CMS"));

		var url = new LinkFunction(context).collectionUrl(itemWithUnnormalizedSlug);

		Assertions.assertThat(url).isEqualTo("/docs/articles/ueber-uns-das-cms");
	}

	@Test
	void usesReloadedCollectionRoute() {
		define("/old/{id}");
		var links = new LinkFunction(context);
		Assertions.assertThat(links.collectionUrl(item)).isEqualTo("/docs/old/item_1");

		define("/new/{slug}");

		Assertions.assertThat(links.collectionUrl(item)).isEqualTo("/docs/new/first-post");
	}

	@Test
	void rejectsItemsWithoutTheConfiguredRouteValue() {
		define("/articles/{slug}");
		var itemWithoutSlug = new CollectionItem(
				"item_2",
				"blog",
				"blog/item_2.md",
				"",
				Map.of());

		Assertions.assertThatIllegalArgumentException()
				.isThrownBy(() -> new LinkFunction(context).collectionUrl(itemWithoutSlug))
				.withMessageContaining("slug");
	}

	@Test
	void createsUrlFromMultipleFormattedAndMappedMetadataValues() {
		definitions.put("blog", new CollectionDefinition(
				"blog",
				new CollectionDetailConfiguration(
						"/events/{date:yyyy}-{date:MM}-{date:dd}/{location.country}/{location.city}",
						"collections/detail.html",
						Map.of("location.country", Map.of("de", "germany")))));
		collectionConfiguration.replaceCollections(definitions);
		var event = new CollectionItem(
				"event_1",
				"blog",
				"blog/event_1.md",
				"",
				Map.of(
						"date", LocalDate.of(2026, 9, 3),
						"location", Map.of("country", "de", "city", "München")));

		Assertions.assertThat(new LinkFunction(context).collectionUrl(event))
				.isEqualTo("/docs/events/2026-09-03/germany/muenchen");
	}

	private static CollectionDefinition definition(String route) {
		return new CollectionDefinition(
				"blog",
				new CollectionDetailConfiguration(route, "collections/detail.html"));
	}

	private void define(String route) {
		definitions.put("blog", definition(route));
		collectionConfiguration.replaceCollections(definitions);
	}
}
