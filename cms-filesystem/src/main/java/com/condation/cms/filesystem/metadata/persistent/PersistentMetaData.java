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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.NodeVisibility;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.filesystem.metadata.AbstractMetaData;
import com.condation.cms.filesystem.metadata.query.ExcerptMapperFunction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class PersistentMetaData extends AbstractMetaData implements AutoCloseable {

	private final Path hostPath;

	private LuceneIndex index;
	private MVStore store;
	private SectionIndex sectionIndex;
	private UrlIndex urlIndex;
	private MVMap<String, ContentNode> nodesByPath;
	
	private TitleQueryFactory titleQueryFactory;

	@Override
	public void open() throws IOException {

		Files.createDirectories(hostPath.resolve("data/metadata/store"));
		Files.createDirectories(hostPath.resolve("data/metadata/index"));

		index = new LuceneIndex();
		index.open(hostPath.resolve("data/metadata/index"));



		store = MVStore.open(hostPath.resolve("data/metadata/store/data.db").toString());

		nodesByPath = store.openMap("nodes");
		nodes = nodesByPath;
		tree = store.openMap("tree");
        urlToUri = store.openMap("urlToUri");
		sectionIndex = new SectionIndex(store);
		urlIndex = new UrlIndex(store, urlToUri);

		nodesByPath.clear();
		tree.clear();
		sectionIndex.clear();
		urlIndex.clear();

		titleQueryFactory = new TitleQueryFactory(LuceneIndex.SEARCH_ANALYZER);
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

	public void startBatch () {
		index.setBatchMode(true);
	}
	
	public void stopBatch () {
		try {
			index.setBatchMode(false);
			index.commit();
		} catch (IOException ex) {
			log.error("error commiting index", ex);
		}
	}

	@Override
	public synchronized void createDirectory(String path) {
		super.createDirectory(path);
	}
	
	@Override
	public synchronized void addFile(String uri, Map<String, Object> data, LocalDate lastModified) {

		var url = PathUtil.toURL(uri);

		if (data.get(Constants.MetaFields.URL) instanceof String configuredUrl && !configuredUrl.isBlank()) {
			url = configuredUrl;
		}
		url = PathUtil.normalizeURL(url);

		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		final ContentNode node = new ContentNode(uri, url, parts[parts.length - 1], data, lastModified);

		nodes.put(uri, node);
		urlIndex.put(node);
		sectionIndex.add(uri);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children().put(node.name(), node);
		} else {
			tree.put(node.name(), node);
		}

		Document document = new Document();
		document.add(new StringField("_uri", uri, Field.Store.YES));
        document.add(new StringField("_url", node.url(), Field.Store.YES));
		//document.add(new StringField("_source", GSON.toJson(node), Field.Store.NO));

		DocumentHelper.addData(document, data);

		DocumentHelper.addSearchFields(document, data);
		
		document.add(new StringField("content.type", node.contentType(), Field.Store.NO));

		try {
			DocumentHelper.addAvailableFields(document);
			this.index.update(new Term("_uri", uri), document);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	@Override
	public synchronized void removeFile(String path) {
		var node = nodesByPath.remove(path);
		if (node == null) {
			return;
		}

		removeNodeFromSecondaryIndexes(node);
		removeFromTree(path);
		try {
			index.delete(new TermQuery(new Term("_uri", path)));
		} catch (IOException ex) {
			log.error("error deleting metadata for {}", path, ex);
		}
	}

	@Override
	public synchronized void removeDirectory(String path) {
		var normalizedPath = stripTrailingSlash(path);
		if (normalizedPath.isEmpty() || getFolder(normalizedPath).isEmpty()) {
			return;
		}

		var prefix = normalizedPath + "/";
		var affectedPaths = pathsWithPrefix(prefix);
		removeFromTree(normalizedPath);
		affectedPaths.forEach(affectedPath -> {
			var node = nodesByPath.remove(affectedPath);
			if (node != null) {
				removeNodeFromSecondaryIndexes(node);
			}
		});

		try {
			index.delete(new PrefixQuery(new Term("_uri", prefix)));
		} catch (IOException ex) {
			log.error("error deleting metadata below {}", normalizedPath, ex);
		}
	}

	@Override
	public synchronized void removePath(String path) {
		var normalizedPath = stripTrailingSlash(path);
		if (nodesByPath.containsKey(normalizedPath)) {
			removeFile(normalizedPath);
		} else {
			removeDirectory(normalizedPath);
		}
	}

	private List<String> pathsWithPrefix(String prefix) {
		var paths = new ArrayList<String>();
		var cursor = nodesByPath.cursor(prefix);
		while (cursor.hasNext()) {
			var path = cursor.next();
			if (!path.startsWith(prefix)) {
				break;
			}
			paths.add(path);
		}
		return paths;
	}

	private void removeNodeFromSecondaryIndexes(ContentNode node) {
		urlIndex.remove(node.path());
		sectionIndex.remove(node.path());
	}

	private static String stripTrailingSlash(String path) {
		var normalized = path.replace('\\', '/');
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	@Override
	public List<ContentNode> listSectionEntries(String pagePath) {
		return sectionIndex.findByPagePath(pagePath).stream()
				.map(nodes::get)
				.filter(node -> node != null)
				.filter(node -> !node.isHidden())
				.filter(NodeVisibility::isVisible)
				.toList();
	}

	@Override
	public synchronized void clear() {
		super.clear();
		sectionIndex.clear();
		urlIndex.clear();
		try {
			index.delete(MatchAllDocsQuery.INSTANCE);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	@Override
	public <T> ContentQuery<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper) {
		return new LuceneQuery<>(this.index, this, new ExcerptMapperFunction<>(nodeMapper));
	}

	@Override
	public <T> ContentQuery<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper) {

		final String uri;
		if (startURI.startsWith("/")) {
			uri = startURI.substring(1);
		} else {
			uri = startURI;
		}
		return new LuceneQuery<>(uri, this.index, this, new ExcerptMapperFunction<>(nodeMapper));
	}

	@Override
	public TitleQuery searchByTitle (String input) {
		return new TitleQuery(titleQueryFactory, input, index, this);
		
	}
}
