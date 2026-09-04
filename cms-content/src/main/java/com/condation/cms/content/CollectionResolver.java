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

import com.condation.cms.api.Constants;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.CollectionDefinition;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.CurrentCollectionItemFeature;
import com.condation.cms.api.feature.features.CurrentNodeFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteDBService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Resolves configured collection detail routes and renders their items.
 */
@RequiredArgsConstructor
public class CollectionResolver {

	private final ContentRenderer contentRenderer;
	private final DB db;
	private final Configuration configuration;

	public Optional<ContentResponse> getContent(RequestContext context) throws IOException {
		var collectionConfiguration = configuration.get(CollectionConfiguration.class);
		if (collectionConfiguration == null) {
			return Optional.empty();
		}
		var route = new CollectionRouteResolver(db, collectionConfiguration)
				.resolve(context.get(RequestFeature.class).uri());
		if (route.isEmpty()) {
			return Optional.empty();
		}
		return render(route.get(), context);
	}

	private Optional<ContentResponse> render(
			CollectionRouteResolver.ResolvedRoute route,
			RequestContext context) throws IOException {
		var collectionItem = route.item();
		var nodeData = new HashMap<>(collectionItem.meta());
		nodeData.put("template", route.detail().template());
		var node = new ContentNode(
				collectionItem.path(),
				route.uri(),
				collectionItem.id() + ".md",
				nodeData);
		context.add(CurrentNodeFeature.class, new CurrentNodeFeature(node));
		context.add(
				CurrentCollectionItemFeature.class,
				new CurrentCollectionItemFeature(collectionItem));

		var sourceDB = sourceDB(route.definition());
		var collectionFile = sourceDB.getFileSystem().collectionsBase().resolve(collectionItem.path());
		if (!collectionFile.exists()) {
			return Optional.empty();
		}
		var content = contentRenderer.renderCollection(
				collectionFile,
				node,
				collectionItem,
				route.detail().template(),
				context);
		return Optional.of(new DefaultContentResponse(content, Constants.DEFAULT_CONTENT_TYPE, node));
	}

	private DB sourceDB(CollectionDefinition definition) {
		var sourceSite = definition.sourceSite();
		if (sourceSite.isEmpty()) {
			return db;
		}
		var siteConfiguration = configuration.get(SiteConfiguration.class);
		if (siteConfiguration != null
				&& siteConfiguration.siteProperties().id().equals(sourceSite.get())) {
			return db;
		}
		return ServiceRegistry.getInstance().get(sourceSite.get(), SiteDBService.class)
				.orElseThrow(() -> new IllegalStateException(
						"collection source site is not available: " + sourceSite.get()))
				.db();
	}
}
