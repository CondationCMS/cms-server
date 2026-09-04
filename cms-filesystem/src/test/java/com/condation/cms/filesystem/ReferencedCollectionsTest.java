package com.condation.cms.filesystem;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteDBService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ReferencedCollectionsTest {

	@AfterEach
	void clearServices() {
		ServiceRegistry.getInstance().clear();
	}

	@Test
	void resolvesReferencedCollectionsThroughTheSourceSite() {
		var local = mock(Collections.class);
		when(local.names()).thenReturn(Set.of("local"));
		var sourceCollections = mock(Collections.class);
		var sourceCollection = mock(Collection.class);
		when(sourceCollections.isLocal("shared")).thenReturn(true);
		when(sourceCollections.collection("shared")).thenReturn(sourceCollection);
		var sourceDB = mock(DB.class);
		when(sourceDB.getCollections()).thenReturn(sourceCollections);
		ServiceRegistry.getInstance().register(
				"content-site",
				SiteDBService.class,
				new SiteDBService(sourceDB));
		var definitions = new ConcurrentHashMap<String, CollectionDefinition>();
		definitions.put("shared", new CollectionDefinition("shared", "content-site", null));
		var collections = new ReferencedCollections(
				"consumer-site",
				local,
				new CollectionConfiguration(definitions));

		Assertions.assertThat(collections.names()).containsExactlyInAnyOrder("local", "shared");
		Assertions.assertThat(collections.isLocal("shared")).isFalse();
		Assertions.assertThat(collections.collection("shared")).isSameAs(sourceCollection);
		Assertions.assertThatThrownBy(() -> collections.refresh("shared", "item"))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("read-only");
	}

	@Test
	void usesReloadedConfigurationWithoutRecreatingTheCollectionsFacade() {
		var local = mock(Collections.class);
		var localCollection = mock(Collection.class);
		when(local.names()).thenReturn(Set.of("shared"));
		when(local.collection("shared")).thenReturn(localCollection);
		var definitions = new ConcurrentHashMap<String, CollectionDefinition>();
		definitions.put("shared", new CollectionDefinition("shared", "content-site", null));
		var configuration = new CollectionConfiguration(definitions);
		var collections = new ReferencedCollections(
				"consumer-site",
				local,
				configuration);

		definitions.put("shared", new CollectionDefinition("shared", null));
		configuration.replaceCollections(definitions);

		Assertions.assertThat(collections.isLocal("shared")).isTrue();
		Assertions.assertThat(collections.collection("shared")).isSameAs(localCollection);
		collections.refresh("shared", "item");
		verify(local).refresh("shared", "item");
	}
}
