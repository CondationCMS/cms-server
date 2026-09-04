package com.condation.cms.filesystem.metadata.persistent;

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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.CursorPage;
import com.condation.cms.filesystem.MetaData;
import com.condation.cms.filesystem.metadata.query.ExcerptMapperFunction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

/**
 * Shared metadata and Lucene index for every collection of one site.
 */
@Slf4j
public class CollectionMetaData implements MetaData {

	private static final String INDEX_SCHEMA_VERSION = "1";
	public static final String FIELD_COLLECTION = "_collection";
	public static final String FIELD_ID = "_id";

	private final Path hostPath;
	private LuceneIndex index;
	private MVStore store;
	private MVMap<String, ContentNode> nodes;
	private MVMap<String, String> fileStamps;
	private MVMap<String, String> settings;
	private boolean batchMode;

	public CollectionMetaData(Path hostPath) {
		this.hostPath = hostPath;
	}

	@Override
	public void open() throws IOException {
		var dataPath = hostPath.resolve("data/collections");
		Files.createDirectories(dataPath.resolve("store"));
		Files.createDirectories(dataPath.resolve("index"));

		store = MVStore.open(dataPath.resolve("store/data.db").toString());
		nodes = store.openMap("nodes");
		fileStamps = store.openMap("file-stamps");
		settings = store.openMap("settings");
		var indexPath = dataPath.resolve("index");
		var recreate = !INDEX_SCHEMA_VERSION.equals(settings.get("index-schema"))
				|| !LuceneIndex.exists(indexPath);
		index = new LuceneIndex();
		index.open(indexPath, recreate);
		if (recreate) {
			nodes.clear();
			fileStamps.clear();
			settings.put("index-schema", INDEX_SCHEMA_VERSION);
			store.commit();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (index != null) {
				index.close();
			}
			if (store != null) {
				store.close();
			}
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public void startBatch() {
		batchMode = true;
		index.setBatchMode(true);
	}

	public void stopBatch() {
		try {
			index.setBatchMode(false);
			index.commit();
			store.commit();
		} catch (IOException ex) {
			log.error("error committing collection index", ex);
		} finally {
			batchMode = false;
		}
	}

	@Override
	public synchronized void addFile(String path, Map<String, Object> data, LocalDate lastModified) {
		addFile(path, data, lastModified, null);
	}

	public synchronized void addFile(
			String path,
			Map<String, Object> data,
			LocalDate lastModified,
			String fileStamp) {
		var normalizedPath = normalize(path);
		var separator = normalizedPath.indexOf('/');
		if (separator <= 0 || separator == normalizedPath.length() - 1) {
			throw new IllegalArgumentException("collection item path must contain collection and filename");
		}

		var collection = normalizedPath.substring(0, separator);
		var filename = normalizedPath.substring(separator + 1);
		var id = filename.endsWith(".md")
				? filename.substring(0, filename.length() - 3)
				: filename;
		var node = new ContentNode(
				normalizedPath,
				normalizedPath,
				filename,
				data,
				lastModified);
		nodes.put(normalizedPath, node);

		var document = new Document();
		document.add(new StringField("_uri", normalizedPath, Field.Store.YES));
		document.add(new StringField(FIELD_COLLECTION, collection, Field.Store.YES));
		document.add(new StringField(FIELD_ID, id, Field.Store.YES));
		DocumentHelper.addData(document, data);
		DocumentHelper.addSearchFields(document, data);
		DocumentHelper.addAvailableFields(document);
		try {
			index.update(new Term("_uri", normalizedPath), document);
			if (fileStamp != null) {
				fileStamps.put(normalizedPath, fileStamp);
			}
			commitStoreIfNecessary();
		} catch (IOException ex) {
			log.error("error indexing collection item {}", normalizedPath, ex);
		}
	}

	@Override
	public synchronized void removeFile(String path) {
		var normalizedPath = normalize(path);
		try {
			index.delete(new TermQuery(new Term("_uri", normalizedPath)));
			nodes.remove(normalizedPath);
			fileStamps.remove(normalizedPath);
			commitStoreIfNecessary();
		} catch (IOException ex) {
			log.error("error deleting collection item {}", normalizedPath, ex);
		}
	}

	@Override
	public synchronized void removeDirectory(String path) {
		var collection = normalize(path);
		var prefix = collection + "/";
		var affectedPaths = new ArrayList<String>();
		var cursor = nodes.cursor(prefix);
		while (cursor.hasNext()) {
			var itemPath = cursor.next();
			if (!itemPath.startsWith(prefix)) {
				break;
			}
			affectedPaths.add(itemPath);
		}
		affectedPaths.forEach(nodes::remove);
		try {
			index.delete(new TermQuery(new Term(FIELD_COLLECTION, collection)));
			affectedPaths.forEach(fileStamps::remove);
			commitStoreIfNecessary();
		} catch (IOException ex) {
			log.error("error deleting collection {}", collection, ex);
		}
	}

	@Override
	public synchronized void removePath(String path) {
		var normalizedPath = normalize(path);
		if (normalizedPath.contains("/")) {
			removeFile(normalizedPath);
		} else {
			removeDirectory(normalizedPath);
		}
	}

	@Override
	public Optional<ContentNode> byUri(String uri) {
		return byPath(uri);
	}

	@Override
	public Optional<ContentNode> byPath(String path) {
		return Optional.ofNullable(nodes.get(normalize(path)));
	}

	@Override
	public Optional<ContentNode> byUrl(String url) {
		return Optional.empty();
	}

	@Override
	public void createDirectory(String path) {
		// Collections are represented by the _collection field, not directory nodes.
	}

	@Override
	public Optional<ContentNode> findFolder(String path) {
		return Optional.empty();
	}

	@Override
	public List<ContentNode> listChildren(String path) {
		var prefix = normalize(path) + "/";
		return nodes.values().stream()
				.filter(node -> node.path().startsWith(prefix))
				.toList();
	}

	@Override
	public List<ContentNode> listSectionEntries(String pagePath) {
		return List.of();
	}

	@Override
	public synchronized void clear() {
		nodes.clear();
		fileStamps.clear();
		try {
			index.delete(MatchAllDocsQuery.INSTANCE);
		} catch (IOException ex) {
			log.error("error clearing collection index", ex);
		}
	}

	public Optional<String> fileStamp(String path) {
		return Optional.ofNullable(fileStamps.get(normalize(path)));
	}

	public Set<String> paths(String collection) {
		var result = new HashSet<String>();
		var prefix = normalize(collection) + "/";
		var cursor = nodes.cursor(prefix);
		while (cursor.hasNext()) {
			var path = cursor.next();
			if (!path.startsWith(prefix)) {
				break;
			}
			result.add(path);
		}
		return result;
	}

	public Set<String> collectionNames() {
		var result = new HashSet<String>();
		for (var path : nodes.keySet()) {
			var separator = path.indexOf('/');
			if (separator > 0) {
				result.add(path.substring(0, separator));
			}
		}
		return result;
	}

	private void commitStoreIfNecessary() {
		if (!batchMode) {
			store.commit();
		}
	}

	@Override
	public Map<String, ContentNode> getNodes() {
		return new ConcurrentHashMap<>(nodes);
	}

	@Override
	public Map<String, ContentNode> getTree() {
		return Map.of();
	}

	@Override
	public <Q> ContentQuery<Q> query(BiFunction<ContentNode, Integer, Q> nodeMapper) {
		return collectionQuery(null, nodeMapper);
	}

	@Override
	public <Q> ContentQuery<Q> query(String collection, BiFunction<ContentNode, Integer, Q> nodeMapper) {
		return collectionQuery(collection, nodeMapper);
	}

	public <Q> CursorPage<Q> cursorPage(
			String collection,
			BiFunction<ContentNode, Integer, Q> nodeMapper,
			String cursor,
			long size,
			Consumer<ContentQuery<Q>> queryConfigurer) {
		var query = collectionQuery(collection, nodeMapper);
		queryConfigurer.accept(query);
		return query.cursorPage(cursor, size);
	}

	private <Q> LuceneQuery<Q> collectionQuery(
			String collection,
			BiFunction<ContentNode, Integer, Q> nodeMapper) {
		var scope = collection == null
				? null
				: new TermQuery(new Term(FIELD_COLLECTION, collection));
		return new LuceneQuery<>(
				index,
				this,
				new ExcerptMapperFunction<>(nodeMapper),
				LuceneQueryPolicy.COLLECTION,
				scope);
	}

	private static String normalize(String path) {
		var normalized = path == null ? "" : path.replace('\\', '/');
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}
}
