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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.collection.CollectionItem;
import com.condation.cms.api.db.collection.CollectionItemMetadata;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.CollectionDetailConfiguration;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.CurrentCollectionItemFeature;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.ui.rpc.RPCException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RemoteContentEndpointsExtensionTest {

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private DB db;

	@Mock
	private Content content;

	@Mock
	private DBFileSystem fileSystem;

	@Mock
	private ReadOnlyFile contentBase;

	@Mock
	private ReadOnlyFile contentFile;

	@Mock
	private Collections collections;

	private RemoteContentEndpointsExtension endpoints;

	@BeforeEach
	void setUp() {
		endpoints = new RemoteContentEndpointsExtension();
		endpoints.setContext(moduleContext);
		when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		lenient().when(db.getFileSystem()).thenReturn(fileSystem);
		lenient().when(fileSystem.contentBase()).thenReturn(contentBase);
		lenient().when(db.getCollections()).thenReturn(collections);
		lenient().when(collections.isLocal("blog")).thenReturn(true);
	}

	@Test
	void getContent_throwsRPCException_whenParsingFails() throws IOException {
		when(contentBase.resolve("broken.md")).thenReturn(contentFile);
		when(contentFile.exists()).thenReturn(true);
		when(contentFile.getContent()).thenThrow(new IOException("disk error"));

		Map<String, Object> params = Map.of("uri", "broken.md");

		assertThatThrownBy(() -> endpoints.getContent(params))
				.isInstanceOf(RPCException.class)
				.hasMessage("disk error");
	}

	@Test
	void setContent_throwsRPCException_whenParsingFails() throws IOException {
		when(contentBase.resolve("broken.md")).thenReturn(contentFile);
		when(contentFile.exists()).thenReturn(true);
		when(contentFile.getContent()).thenThrow(new IOException("disk error"));

		Map<String, Object> params = Map.of("uri", "broken.md", "content", "hello");

		assertThatThrownBy(() -> endpoints.setContent(params))
				.isInstanceOf(RPCException.class)
				.hasMessage("disk error");
	}

	@Test
	void getContentUsesCurrentNodeWhenUriIsOmitted() throws Exception {
		var uri = ".variants/about/summer/about.md";
		var requestContext = new RequestContext();
		requestContext.add(
				CurrentNodeFeature.class,
				new CurrentNodeFeature(new ContentNode(uri, "/about", "about.md", Map.of()))
		);
		when(contentBase.resolve(uri)).thenReturn(contentFile);
		when(contentFile.exists()).thenReturn(true);
		when(contentFile.getContent()).thenThrow(new IOException("variant selected"));

		assertThatThrownBy(() -> ScopedValue.where(
				RequestContextScope.REQUEST_CONTEXT,
				requestContext
		).call(() -> endpoints.getContent(Map.of())))
				.isInstanceOf(RPCException.class)
				.hasMessage("variant selected");
	}

	@Test
	void rejectsEditingAReferencedCollectionItem() {
		when(collections.isLocal("blog")).thenReturn(false);
		var requestContext = new RequestContext();
		requestContext.add(
				CurrentCollectionItemFeature.class,
				new CurrentCollectionItemFeature(new CollectionItem(
						"first", "blog", "blog/first.md", "Body", Map.of())));

		assertThatThrownBy(() -> ScopedValue.where(
				RequestContextScope.REQUEST_CONTEXT,
				requestContext).call(() -> endpoints.getContent(Map.of())))
				.isInstanceOfSatisfying(RPCException.class, exception ->
						assertThat(exception.getCode()).isEqualTo(403));
	}

	@Test
	void getContentNodeResolvesUriFromCustomPageUrl() throws Exception {
		var node = new ContentNode(
				"pages/other.md",
				"/total-other-page",
				"other.md",
				Map.of()
		);
		when(db.getContent()).thenReturn(content);
		when(content.byUrl("/total-other-page")).thenReturn(Optional.of(node));
		when(contentBase.resolve("pages/other.md")).thenReturn(contentFile);
		when(content.listSectionEntries(contentFile)).thenReturn(List.of());

		var requestContext = new RequestContext();
		requestContext.add(
				RequestFeature.class,
				new RequestFeature("/", "/total-other-page", Map.of(), null)
		);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) ScopedValue.where(
				RequestContextScope.REQUEST_CONTEXT,
				requestContext
		).call(() -> endpoints.getContentNode(Map.of(
				"url", "https://example.test/total-other-page?preview=manager"
		)));

		assertThat(result)
				.containsEntry("uri", "pages/other.md")
				.containsEntry("canonicalUri", "pages/other.md")
				.containsEntry("contentKind", "content")
				.containsEntry("supportsVariants", true);
		verify(content).byUrl("/total-other-page");
	}

	@Test
	void getContentNodeKeepsPublicCollectionRouteAndDisablesVariants() throws Exception {
		var nonExistingPath = mock(ReadOnlyFile.class);
		var authorCollection = mock(Collection.class);
		@SuppressWarnings("unchecked")
		var query = (ContentQuery<CollectionItemMetadata>) mock(ContentQuery.class);
		var item = new CollectionItem(
				"author-1", "authors", "authors/author-1.md", "", Map.of("slug", "jane-doe"));
		var metadata = new CollectionItemMetadata(
				item.id(), item.collection(), item.path(), item.meta());

		when(db.getContent()).thenReturn(content);
		when(content.byUrl("/people/jane-doe")).thenReturn(Optional.empty());
		when(contentBase.resolve("people/jane-doe")).thenReturn(nonExistingPath);
		when(contentBase.resolve("people/jane-doe.md")).thenReturn(nonExistingPath);
		when(collections.collection("authors")).thenReturn(authorCollection);
		when(authorCollection.metadataQuery()).thenReturn(query);
		when(query.where("slug", "jane-doe")).thenReturn(query);
		when(query.page(1, 2)).thenReturn(new Page<>(1, 2, 1, 1, List.of(metadata)));
		when(authorCollection.item("author-1")).thenReturn(Optional.of(item));

		var configuration = new Configuration();
		configuration.add(CollectionConfiguration.class, new CollectionConfiguration(
				new ConcurrentHashMap<>(Map.of(
						"authors",
						new CollectionDefinition(
								"authors",
								new CollectionDetailConfiguration("/people/{slug}", "author.html"))))));
		when(moduleContext.get(ConfigurationFeature.class))
				.thenReturn(new ConfigurationFeature(configuration));

		var requestContext = new RequestContext();
		requestContext.add(RequestFeature.class, new RequestFeature("/", "/people/jane-doe", Map.of(), null));

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) ScopedValue.where(
				RequestContextScope.REQUEST_CONTEXT,
				requestContext
		).call(() -> endpoints.getContentNode(Map.of(
				"url", "https://example.test/people/jane-doe?preview=manager")));

		assertThat(result)
				.containsEntry("url", "https://example.test/people/jane-doe?preview=manager")
				.containsEntry("uri", "authors/author-1.md")
				.containsEntry("contentKind", "collection")
				.containsEntry("supportsVariants", false);
	}
}
