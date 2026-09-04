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

import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.CursorPage;
import com.condation.cms.api.db.collection.Collection;
import com.condation.cms.api.db.collection.CollectionCursorSupport;
import com.condation.cms.api.db.collection.CollectionItemMetadata;
import com.condation.cms.api.db.collection.Collections;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteDBService;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Adds lazy, read-only collection references to the collections stored by one
 * site. Source sites are looked up for every access so configuration and site
 * reloads do not leave cached cross-site references behind.
 */
final class ReferencedCollections implements Collections, CollectionCursorSupport {

	private final String siteId;
	private final Collections localCollections;
	private final CollectionConfiguration configuration;

	ReferencedCollections(
			String siteId,
			Collections localCollections,
			CollectionConfiguration configuration) {
		this.siteId = siteId;
		this.localCollections = localCollections;
		this.configuration = configuration;
	}

	@Override
	public Collection collection(String name) {
		return targetCollections(name).collection(name);
	}

	@Override
	public CursorPage<CollectionItemMetadata> metadataCursorPage(
			String collection,
			String cursor,
			long size,
			Consumer<ContentQuery<CollectionItemMetadata>> queryConfigurer) {
		var target = targetCollections(collection);
		if (!(target instanceof CollectionCursorSupport cursorSupport)) {
			throw new UnsupportedOperationException("collection storage does not support cursor paging");
		}
		return cursorSupport.metadataCursorPage(collection, cursor, size, queryConfigurer);
	}

	@Override
	public Set<String> names() {
		var names = new HashSet<>(localCollections.names());
		configuration.collections().values().stream()
				.filter(definition -> definition.sourceSite()
						.filter(sourceSite -> !siteId.equals(sourceSite))
						.isPresent())
				.map(definition -> definition.name())
				.forEach(names::add);
		return Set.copyOf(names);
	}

	@Override
	public boolean isLocal(String collection) {
		return sourceSite(collection) == null;
	}

	@Override
	public void refresh(String collection, String id) {
		if (!isLocal(collection)) {
			throw new UnsupportedOperationException(
					"referenced collection is read-only: " + collection);
		}
		localCollections.refresh(collection, id);
	}

	private String sourceSite(String collection) {
		return configuration.collection(collection)
				.flatMap(definition -> definition.sourceSite())
				.filter(source -> !siteId.equals(source))
				.orElse(null);
	}

	private Collections targetCollections(String collection) {
		var sourceSite = sourceSite(collection);
		if (sourceSite == null) {
			return localCollections;
		}
		var source = ServiceRegistry.getInstance().get(sourceSite, SiteDBService.class)
				.orElseThrow(() -> new IllegalStateException(
						"collection source site is not available: " + sourceSite));
		var sourceCollections = source.db().getCollections();
		if (!sourceCollections.isLocal(collection)) {
			throw new IllegalStateException(
					"referenced collections must point to a local collection: "
					+ sourceSite + "/" + collection);
		}
		return sourceCollections;
	}
}
