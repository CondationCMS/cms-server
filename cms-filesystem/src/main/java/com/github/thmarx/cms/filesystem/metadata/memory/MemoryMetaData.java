package com.github.thmarx.cms.filesystem.metadata.memory;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.filesystem.metadata.AbstractMetaData;
import com.google.common.base.Strings;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class MemoryMetaData extends AbstractMetaData implements MetaData {

	private final ConcurrentMap<String, ContentNode> nodes = new ConcurrentHashMap<>();

	private final ConcurrentMap<String, ContentNode> tree = new ConcurrentHashMap<>();
	
	@Override
	public void clear() {
		nodes.clear();
		tree.clear();
	}
	
	@Override
	public ConcurrentMap<String, ContentNode> nodes() {
		return new ConcurrentHashMap<>(nodes);
	}

	@Override
	public ConcurrentMap<String, ContentNode> tree() {
		return new ConcurrentHashMap<>(tree);
	}

	@Override
	public void createDirectory(final String uri) {
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
	public void addFile(final String uri, final Map<String, Object> data, final LocalDate lastModified) {

		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		final ContentNode node = new ContentNode(uri, parts[parts.length - 1], data, lastModified);

		nodes.put(uri, node);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children().put(node.name(), node);
		} else {
			tree.put(node.name(), node);
		}
	}

	@Override
	public Optional<ContentNode> byUri(final String uri) {
		if (!nodes.containsKey(uri)) {
			return Optional.empty();
		}
		return Optional.of(nodes.get(uri));
	}

	void remove(String uri) {
		var node = nodes.remove(uri);

		var folder = getFolder(uri);
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		var name = parts[parts.length - 1];
		if (folder.isPresent()) {
			folder.get().children().remove(name);
		} else {
			tree.remove(name);
		}
	}

	@Override
	public void open() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}
	
	@Override
	public <T> ContentQuery<T> query(final BiFunction<ContentNode, Integer, T> nodeMapper) {
		return new MemoryQuery(new ArrayList<>(nodes.values()), nodeMapper);
	}

	@Override
	public <T> ContentQuery<T> query(final String startURI, final BiFunction<ContentNode, Integer, T> nodeMapper) {

		final String uri;
		if (startURI.startsWith("/")) {
			uri = startURI.substring(1);
		} else {
			uri = startURI;
		}

		var filtered = nodes().values().stream().filter(node -> node.uri().startsWith(uri)).toList();

		return new MemoryQuery(filtered, nodeMapper);
	}

	
}
