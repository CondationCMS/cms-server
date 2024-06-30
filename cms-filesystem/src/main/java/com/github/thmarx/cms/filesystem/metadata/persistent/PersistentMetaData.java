package com.github.thmarx.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.ContentQuery;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.filesystem.metadata.memory.MemoryMetaData;
import com.github.thmarx.cms.filesystem.metadata.query.ExcerptMapperFunction;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class PersistentMetaData implements AutoCloseable, MetaData {

	private final Path hostPath;

	private LuceneIndex index;
	private MVStore store;

	private static final Gson GSON = new Gson();

	MVMap<String, ContentNode> nodes;
	MVMap<String, ContentNode> tree;

	@Override
	public void open() throws IOException {

		Files.createDirectories(hostPath.resolve("data/store"));
		Files.createDirectories(hostPath.resolve("data/index"));

		index = new LuceneIndex();
		index.open(hostPath.resolve("data/index"));

		store = MVStore.open(hostPath.resolve("data/store/data").toString());

		nodes = store.openMap("nodes");
		tree = store.openMap("tree");
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

	@Override
	public void addFile(String uri, Map<String, Object> data, LocalDate lastModified) {

		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		final ContentNode node = new ContentNode(uri, parts[parts.length - 1], data, lastModified);

		nodes.put(uri, node);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children().put(node.name(), node);
		} else {
			tree.put(node.name(), node);
		}

		Document document = new Document();
		document.add(new StringField("_uri", uri, Field.Store.YES));
		//document.add(new StringField("_source", GSON.toJson(node), Field.Store.NO));

		DocumentHelper.addData(document, data);

		document.add(new StringField("content.type", node.contentType(), Field.Store.NO));

		try {
			this.index.update(new Term("_uri", uri), document);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}
	
	@Override
	public Optional<ContentNode> byUri(String uri) {
		if (!nodes.containsKey(uri)) {
			return Optional.empty();
		}
		return Optional.of(nodes.get(uri));
	}

	@Override
	public void createDirectory(String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			return;
		}
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		ContentNode n = new ContentNode(uri, parts[parts.length - 1], Map.of(), true);

		Optional<ContentNode> parentFolder;
		if (parts.length == 1) {
			parentFolder = getFolder(uri);
		} else {
			var parentPath = Arrays.copyOfRange(parts, 0, parts.length - 1);
			var parentUri = String.join("/", parentPath);
			parentFolder = getFolder(parentUri);
		}

		if (parentFolder.isPresent()) {
			parentFolder.get().children().put(n.name(), n);
		} else {
			tree.put(n.name(), n);
		}
	}

	@Override
	public Optional<ContentNode> findFolder(String uri) {
		return getFolder(uri);
	}

	private Optional<ContentNode> getFolder(String uri) {
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);

		final AtomicReference<ContentNode> folder = new AtomicReference<>(null);
		Stream.of(parts).forEach(part -> {
			if (part.endsWith(".md")) {
				return;
			}
			if (folder.get() == null) {
				folder.set(tree.get(part));
			} else {
				folder.set(folder.get().children().get(part));
			}
		});
		return Optional.ofNullable(folder.get());
	}

	@Override
	public List<ContentNode> listChildren(String uri) {
		if ("".equals(uri)) {
			return tree.values().stream()
					.filter(node -> !node.isHidden())
					.map(this::mapToIndex)
					.filter(node -> node != null)
					.filter(MemoryMetaData::isVisible)
					.collect(Collectors.toList());

		} else {
			Optional<ContentNode> findFolder = findFolder(uri);
			if (findFolder.isPresent()) {
				return findFolder.get().children().values()
						.stream()
						.filter(node -> !node.isHidden())
						.map(this::mapToIndex)
						.filter(node -> node != null)
						.filter(MemoryMetaData::isVisible)
						.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	protected ContentNode mapToIndex(ContentNode node) {
		if (node.isDirectory()) {
			var tempNode = node.children().entrySet().stream().filter((entry)
					-> entry.getKey().equals("index.md")
			).findFirst();
			if (tempNode.isPresent()) {
				return tempNode.get().getValue();
			}
			return null;
		} else {
			return node;
		}
	}

	@Override
	public void clear() {
		try {
			nodes.clear();
			tree.clear();
			index.delete(new MatchAllDocsQuery());
		} catch (IOException ex) {
			log.error("", ex);
		}
	}

	@Override
	public Map<String, ContentNode> nodes() {
		return nodes;
	}

	@Override
	public Map<String, ContentNode> tree() {
		return tree;
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

}
