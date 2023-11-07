package com.github.thmarx.cms.filesystem;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.utils.SectionUtil;
import com.google.common.base.Strings;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author t.marx
 */
public class MetaData {

	private ConcurrentMap<String, MetaNode> nodes = new ConcurrentHashMap<>();

	private ConcurrentMap<String, MetaNode> tree = new ConcurrentHashMap<>();

	void clear() {
		nodes.clear();
		tree.clear();
	}

	ConcurrentMap<String, MetaNode> nodes() {
		return new ConcurrentHashMap<>(nodes);
	}

	ConcurrentMap<String, MetaNode> tree() {
		return new ConcurrentHashMap<>(tree);
	}

	public void createDirectory(final String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			return;
		}
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		MetaNode n = new MetaNode(uri, parts[parts.length - 1], Map.of(), true);

		Optional<MetaNode> parentFolder;
		if (parts.length == 1) {
			parentFolder = getFolder(uri);
		} else {
			var parentPath = Arrays.copyOfRange(parts, 0, parts.length - 1);
			var parentUri = String.join("/", parentPath);
			parentFolder = getFolder(parentUri);
		}

		if (parentFolder.isPresent()) {
			parentFolder.get().children.put(n.name(), n);
		} else {
			tree.put(n.name(), n);
		}
	}

	public List<MetaNode> listChildren(String uri) {
		if ("".equals(uri)) {
			return tree.values().stream()
					.filter(node -> !node.isHidden())
					.map(node -> {
						if (node.isDirectory) {
							var tempNode = node.children.entrySet().stream().filter((entry)
									-> entry.getKey().equals("index.md")
							).findFirst();
							if (tempNode.isPresent()) {
								return tempNode.get().getValue();
							}
							return null;
						} else {
							return node;
						}
					})
					.filter(node -> node != null)
					.filter(node -> !node.isHidden())
					.filter(node -> node.isPublished())
					.filter(node -> !node.isSection())
					.collect(Collectors.toList());

		} else {
			Optional<MetaData.MetaNode> findFolder = findFolder(uri);
			if (findFolder.isPresent()) {
				return findFolder.get().children().values()
						.stream()
						.filter(node -> !node.isHidden())
						.map(node -> {
							if (node.isDirectory) {
								var tempNode = node.children.entrySet().stream().filter((entry)
										-> entry.getKey().equals("index.md")
								).findFirst();
								if (tempNode.isPresent()) {
									return tempNode.get().getValue();
								}
								return null;
							} else {
								return node;
							}
						})
						.filter(node -> node != null)
						.filter(node -> !node.isHidden())
						.filter(node -> node.isPublished())
						.filter(node -> !node.isSection())
						.collect(Collectors.toList());
			}
		}
		return Collections.emptyList();
	}

	public Optional<MetaNode> findFolder(String uri) {
		return getFolder(uri);
	}

	private Optional<MetaNode> getFolder(String uri) {
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);

		final AtomicReference<MetaNode> folder = new AtomicReference<>(null);
		Stream.of(parts).forEach(part -> {
			if (part.endsWith(".md")) {
				return;
			}
			if (folder.get() == null) {
				folder.set(tree.get(part));
			} else {
				folder.set(folder.get().children.get(part));
			}
		});
		return Optional.ofNullable(folder.get());
	}

	public void addFile(final String uri, final Map<String, Object> data) {

		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		final MetaNode node = new MetaNode(uri, parts[parts.length - 1], data);

		nodes.put(uri, node);

		var folder = getFolder(uri);
		if (folder.isPresent()) {
			folder.get().children.put(node.name(), node);
		} else {
			tree.put(node.name(), node);
		}
	}

	public Optional<MetaNode> byUri(final String uri) {
		if (!nodes.containsKey(uri)) {
			return Optional.empty();
		}
		return Optional.of(nodes.get(uri));
	}

	void remove(String uri) {
		nodes.remove(uri);

		var folder = getFolder(uri);
		var parts = uri.split(Constants.SPLIT_PATH_PATTERN);
		var name = parts[parts.length - 1];
		if (folder.isPresent()) {
			folder.get().children.remove(name);
		} else {
			tree.remove(name);
		}
	}

	public static record MetaNode(String uri, String name, Map<String, Object> data, boolean isDirectory, Map<String, MetaNode> children) {

		public MetaNode(String uri, String name, Map<String, Object> data, boolean isDirectory) {
			this(uri, name, data, isDirectory, new HashMap<String, MetaNode>());
		}

		public MetaNode(String uri, String name, Map<String, Object> data) {
			this(uri, name, data, false, new HashMap<String, MetaNode>());
		}

		public boolean isHidden() {
			return name.startsWith(".");
		}

		public boolean isDraft() {
			return (boolean) data().getOrDefault(Constants.MetaFields.DRAFT, false);
		}

		public boolean isPublished() {
			var localDate = (Date) data.getOrDefault(Constants.MetaFields.PUBLISHED, Date.from(Instant.now()));
			var now = Date.from(Instant.now());
			return !isDraft() && (localDate.before(now) || localDate.equals(now));
		}

		public boolean isSection() {
			return SectionUtil.isSection(name);
		}
	}
}
