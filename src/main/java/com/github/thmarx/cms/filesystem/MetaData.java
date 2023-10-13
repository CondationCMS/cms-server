/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.filesystem;

import com.github.thmarx.cms.Constants;
import com.google.common.base.Strings;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
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
			var parentPath = Arrays.copyOfRange(parts, 0, parts.length-1);
			var parentUri = String.join("/", parentPath);
			parentFolder = getFolder(parentUri);
		}
		
		
		
		if (parentFolder.isPresent()) {
			parentFolder.get().children.put(n.name(), n);
		} else {
			tree.put(n.name(), n);
		}
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
		
		public boolean isHidden () {
			return name.startsWith(".");
		}
		
		public boolean isDraft () {
			return (boolean) data().getOrDefault("draft", false);
		}
		
		public boolean isPublished () {
			var localDate = (Date)data.getOrDefault("published", Date.from(Instant.now()));
			var now = Date.from(Instant.now());
			return !isDraft() && (localDate.before(now) || localDate.equals(now));
		}
		
		public boolean isSection () {
			return Constants.SECTION_PATTERN.matcher(name).matches();
		}
	}
}
