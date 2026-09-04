package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.db.collection.CollectionItemMetadata;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.api.workflow.Workflow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class RemoteCollectionEndpointsTest {

	@TempDir
	Path tempDirectory;

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private DB db;

	@Mock
	private DBFileSystem fileSystem;

	@Mock
	private Collections collections;

	@Mock
	private Collection collection;

	@Mock
	private ContentQuery<CollectionItem> collectionQuery;

	@Mock
	private ContentQuery<CollectionItemMetadata> metadataQuery;

	@Mock
	private EventBus eventBus;

	@Mock
	private Workflow workflow;

	@Mock
	private WFStatusProvider statusProvider;

	private RemoteCollectionEndpoints endpoints;
	private Path collectionsDirectory;

	@BeforeEach
	void setUp() throws Exception {
		collectionsDirectory = tempDirectory.resolve(Constants.Folders.COLLECTIONS);
		Files.createDirectories(collectionsDirectory.resolve("blog"));
		endpoints = new RemoteCollectionEndpoints();
		endpoints.setContext(moduleContext);

		when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		lenient().when(db.getFileSystem()).thenReturn(fileSystem);
		lenient().when(db.getCollections()).thenReturn(collections);
		lenient().when(collections.names()).thenReturn(Set.of("blog"));
		lenient().when(collections.isLocal("blog")).thenReturn(true);
		lenient().when(collections.collection("blog")).thenReturn(collection);
		lenient().when(collection.query()).thenReturn(collectionQuery);
		lenient().when(collection.metadataQuery()).thenReturn(metadataQuery);
		lenient().when(collectionQuery.get()).thenReturn(List.of());
		lenient().when(metadataQuery.where(anyString(), any())).thenReturn(metadataQuery);
		lenient().when(metadataQuery.page(1, 2)).thenReturn(new com.condation.cms.api.db.Page<>(
				0, 2, 0, 1, List.of()));
		lenient().when(fileSystem.resolve(Constants.Folders.COLLECTIONS)).thenReturn(collectionsDirectory);
	}

	@Test
	void createsCollectionItemWithWorkflowMetadataAndRefreshesIndex() throws Exception {
		when(moduleContext.get(EventBusFeature.class)).thenReturn(new EventBusFeature(eventBus));
		when(moduleContext.get(WorkflowFeature.class)).thenReturn(new WorkflowFeature(workflow));
		when(workflow.getStatusProvider()).thenReturn(statusProvider);
		when(statusProvider.newNodeStatus()).thenReturn("draft");
		var requestContext = requestContext();
		var parameters = Map.<String, Object>of(
				"collection", "blog",
				"id", "first-item",
				"content", Map.of("type", "markdown", "value", "# Body"),
				"meta", Map.of(
						"title", Map.of("type", "text", "value", "First item"),
						"slug", Map.of("type", "text", "value", "Über das CMS")));

		var result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext)
				.call(() -> endpoints.create(parameters));

		assertThat(result).isInstanceOfSatisfying(
				RemoteCollectionEndpoints.ItemDto.class,
				item -> {
					assertThat(item)
							.extracting(
									RemoteCollectionEndpoints.ItemDto::id,
									RemoteCollectionEndpoints.ItemDto::title)
							.containsExactly("first-item", "First item");
					assertThat(item.detailUrl()).isEqualTo("/articles/ueber-das-cms");
				});
		assertThat(collectionsDirectory.resolve("blog/first-item.md"))
				.content()
				.contains(
						"title: First item",
						"slug: ueber-das-cms",
						"status: draft",
						"createdBy: editor",
						"# Body");
		verify(collections).refresh("blog", "first-item");
	}

	@Test
	void rejectsDuplicateSlugsAfterNormalizationWhenSaving() throws Exception {
		var editedItem = new CollectionItem(
				"second",
				"blog",
				"blog/second.md",
				"",
				Map.of("slug", "second"));
		var existingItem = new CollectionItemMetadata(
				"first",
				"blog",
				"blog/first.md",
				Map.of("slug", "ueber-uns"));
		var collectionsBase = mock(ReadOnlyFile.class);
		var sourceFile = mock(ReadOnlyFile.class);
		when(collection.item("second")).thenReturn(Optional.of(editedItem));
		when(fileSystem.collectionsBase()).thenReturn(collectionsBase);
		when(collectionsBase.resolve("blog/second.md")).thenReturn(sourceFile);
		when(sourceFile.getContent()).thenReturn("---\nslug: second\n---\n\nBody\n");
		when(metadataQuery.where("slug", "ueber-uns")).thenReturn(metadataQuery);
		when(metadataQuery.page(1, 2)).thenReturn(new com.condation.cms.api.db.Page<>(
				1, 2, 1, 1, List.of(existingItem)));

		assertThatThrownBy(() -> endpoints.save(Map.of(
				"collection", "blog",
				"id", "second",
				"meta", Map.of("slug", Map.of("type", "text", "value", "Ueber uns")))))
				.isInstanceOfSatisfying(
						RPCException.class,
						exception -> assertThat(exception.getCode()).isEqualTo(409));
	}

	@Test
	void rejectsDuplicateAndInvalidCollectionItemIds() throws Exception {
		Files.writeString(collectionsDirectory.resolve("blog/existing.md"), "existing");

		assertThatThrownBy(() -> endpoints.create(Map.of("collection", "blog", "id", "existing")))
				.isInstanceOfSatisfying(
						RPCException.class,
						exception -> assertThat(exception.getCode()).isEqualTo(409));
		assertThatThrownBy(() -> endpoints.create(Map.of("collection", "blog", "id", "../unsafe")))
				.isInstanceOfSatisfying(
						RPCException.class,
						exception -> assertThat(exception.getCode()).isEqualTo(400));
	}

	@Test
	void rejectsInvalidItemIdsConsistentlyWhenLoadingItems() {
		assertThatThrownBy(() -> endpoints.get(Map.of("collection", "blog", "id", "invalid item")))
				.isInstanceOfSatisfying(
						RPCException.class,
						exception -> {
							assertThat(exception.getCode()).isEqualTo(400);
							assertThat(exception.getMessage())
									.isEqualTo("invalid collection item id: invalid item");
						});
	}

	@Test
	void deletesCollectionItemAndRefreshesIndex() throws Exception {
		when(moduleContext.get(EventBusFeature.class)).thenReturn(new EventBusFeature(eventBus));
		var item = collectionsDirectory.resolve("blog/obsolete.md");
		Files.writeString(item, "obsolete");

		endpoints.delete(Map.of("collection", "blog", "id", "obsolete"));

		assertThat(item).doesNotExist();
		verify(collections).refresh("blog", "obsolete");
	}

	@Test
	void rejectsWritesToReferencedCollections() {
		when(collections.isLocal("blog")).thenReturn(false);

		assertThatThrownBy(() -> endpoints.create(Map.of("collection", "blog", "id", "new-item")))
				.isInstanceOfSatisfying(
						RPCException.class,
						exception -> assertThat(exception.getCode()).isEqualTo(403));
	}

	private RequestContext requestContext() {
		var configuration = new Configuration();
		var definitions = new ConcurrentHashMap<String, CollectionDefinition>();
		definitions.put("blog", new CollectionDefinition(
				"blog",
				new CollectionDetailConfiguration("/articles/{slug}", "article.html")));
		configuration.add(CollectionConfiguration.class, new CollectionConfiguration(definitions));
		var siteProperties = mock(SiteProperties.class);
		when(siteProperties.contextPath()).thenReturn("/");
		var requestContext = new RequestContext();
		requestContext.add(AuthFeature.class, new AuthFeature("editor"));
		requestContext.add(ConfigurationFeature.class, new ConfigurationFeature(configuration));
		requestContext.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
		return requestContext;
	}
}
