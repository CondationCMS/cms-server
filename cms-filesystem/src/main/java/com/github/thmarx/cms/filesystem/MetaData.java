package com.github.thmarx.cms.filesystem;

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
import com.github.thmarx.cms.filesystem.index.IndexProviding;
import com.github.thmarx.cms.filesystem.index.SecondaryIndex;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class MetaData implements IndexProviding {

	private ConcurrentMap<String, ContentNode> nodes = new ConcurrentHashMap<>();

	private ConcurrentMap<String, ContentNode> tree = new ConcurrentHashMap<>();
	
	private ConcurrentMap<String, SecondaryIndex<?>> secondaryIndexes = new ConcurrentHashMap<>();
	
	@Override
	public SecondaryIndex<?> getOrCreateIndex (final String field, Function<ContentNode, Object> indexFunction) {
		
		if (!secondaryIndexes.containsKey(field)) {
			var index = SecondaryIndex.<Object>builder()
					.indexFunction(indexFunction)
					.build();
			index.addAll(nodes.values());
			secondaryIndexes.put(field, index);
		}
		
		return secondaryIndexes.get(field);
	}
	
	void clear() {
		nodes.clear();
		tree.clear();
		secondaryIndexes.clear();
	}
	
	ConcurrentMap<String, ContentNode> nodes() {
		return new ConcurrentHashMap<>(nodes);
	}

	ConcurrentMap<String, ContentNode> tree() {
		return new ConcurrentHashMap<>(tree);
	}

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

	public List<ContentNode> listChildren(String uri) {
		if ("".equals(uri)) {
			return tree.values().stream()
					.filter(node -> !node.isHidden())
					.map(this::mapToIndex)
					.filter(node -> node != null)
					.filter(MetaData::isVisible)
					.collect(Collectors.toList());

		} else {
			Optional<ContentNode> findFolder = findFolder(uri);
			if (findFolder.isPresent()) {
				return findFolder.get().children().values()
						.stream()
						.filter(node -> !node.isHidden())
						.map(this::mapToIndex)
						.filter(node -> node != null)
						.filter(MetaData::isVisible)
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

	public static boolean isVisible (ContentNode node) {
		return node != null 
				// check if some parent is hidden
				&& !node.uri().startsWith(".") && !node.uri().contains("/.")
				&& node.isPublished() 
				&& !node.isHidden() 
				&& !node.isSection();
	}
	
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
		
		secondaryIndexes.values().forEach(index -> index.add(node));
	}

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
		
		secondaryIndexes.values().forEach(index -> index.remove(node));
	}

	
}
