package com.github.thmarx.cms.template.functions.list;

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
import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.template.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.utils.NodeUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
class NodeListFunction extends AbstractCurrentNodeFunction {

	public static int DEFAULT_PAGE = 1;
	public static int DEFAUTL_PAGE_SIZE = 5;

	private boolean excludeIndexMd = false;

	private Predicate<MetaData.MetaNode> nodeNameFilter = (node) -> {
		var filename = node.name();
		if (excludeIndexMd && "index.md".equals(filename)) {
			return false;
		}
		return true;
	};

	public NodeListFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser, MarkdownRenderer markdownRenderer) {
		super(fileSystem, currentNode, contentParser, markdownRenderer);
	}

	public NodeListFunction(FileSystem fileSystem, Path currentNode, ContentParser contentParser, MarkdownRenderer markdownRenderer, boolean excludeIndexMd) {
		this(fileSystem, currentNode, contentParser, markdownRenderer);
		this.excludeIndexMd = excludeIndexMd;
	}

	public Page<Node> list(String start, int page, int size, final Comparator<MetaData.MetaNode> comparator) {
		return getNodes(start, page, size, comparator);
	}

	Page<Node> getNodes(final String start, int page, int size, final Comparator<MetaData.MetaNode> comparator) {

		Path baseNode = null;
		String path = start;
		// first select base node
		if (start.startsWith("/")) {
			baseNode = fileSystem.resolve("content/");
			path = start.substring(1);
		} else if (start.equals(".")) {
			baseNode = currentNode.getParent();
			path = "";
		} else if (start.startsWith("./")) {
			baseNode = currentNode.getParent();
			path = start.substring(2);
		}

		if (baseNode == null) {
			return Page.EMPTY;
		}

		/*
		path:
		blog/2023-12/
		blog\/*\/*
		 */
		if (path.contains("*")) {
			final Path contentBase = fileSystem.resolve("content/");
			List<MetaData.MetaNode> relevantPaths = getPaths(baseNode, path);

			List<MetaData.MetaNode> allContentNodes = new ArrayList<>();
			relevantPaths.forEach((metaNode) -> {

				List<MetaData.MetaNode> children = fileSystem.listContent(contentBase, metaNode.uri());
				allContentNodes.addAll(children);
			});

			long total = allContentNodes.stream().filter(nodeNameFilter).count();
			int skipCount = (page - 1) * size;

			List<Node> navNodes = new ArrayList<>();
			allContentNodes.stream().filter(nodeNameFilter)
					.sorted(comparator)
					.skip(skipCount)
					.limit(size)
					.forEach(node -> {
						var temp_path = contentBase.resolve(node.uri());
						var name = NodeUtil.getName(node);
						var md = parse(temp_path);
						var excerpt = markdownRenderer.excerpt(md.get().content(), 200);
						final Node navNode = new Node(name, getUrl(temp_path), excerpt, node.data());
						navNodes.add(navNode);
					});

			int totalPages = (int) Math.ceil((float) total / size);
			return new Page<Node>(navNodes.size(), totalPages, page, navNodes);

		} else {
			return getNodesFromBase(baseNode, path, page, size, comparator);
		}
	}

	private List<MetaData.MetaNode> getPaths(final Path base, final String path) {
		Set<MetaData.MetaNode> relevantPaths = new HashSet<>();
		var parts = path.split(Constants.SPLIT_PATH_PATTERN);

		var part = parts[0];
		List<MetaData.MetaNode> nodes;
		if ("*".equals(part)) {
			nodes = fileSystem.listDirectories(base, "");
		} else {
			nodes = fileSystem.listDirectories(base, part);
		}
		if (parts.length > 1) {
			nodes.forEach((node) -> {
				var newPath = Arrays.copyOfRange(parts, 1, parts.length);
				var subnodes = getPaths(base.resolve(part), String.join("/", newPath));
				relevantPaths.addAll(subnodes);
			});
		}
		if (parts.length == 1) {
			relevantPaths.addAll(nodes);
		}

		return new ArrayList<>(relevantPaths);
	}

	public Page<Node> getNodesFromBase(final Path base, final String start, final int page, final int pageSize, final Comparator<MetaData.MetaNode> comparator) {
		try {
			List<Node> nodes = new ArrayList<>();
			final List<MetaData.MetaNode> navNodes = fileSystem.listContent(base, start);
			final Path contentBase = fileSystem.resolve("content/");
			long total = navNodes.stream().filter(nodeNameFilter).count();
			int skipCount = (page - 1) * pageSize;

			navNodes.stream().filter(nodeNameFilter)
					.sorted(comparator)
					.skip(skipCount)
					.limit(pageSize)
					.forEach(node -> {
						var path = contentBase.resolve(node.uri());
						var name = NodeUtil.getName(node);
						var md = parse(path);
						final Node navNode = new Node(name, getUrl(path), md.get().content(), node.data());
						nodes.add(navNode);
					});

			int totalPages = (int) Math.ceil((float) total / pageSize);
			return new Page<Node>(pageSize, totalPages, page, nodes);
		} catch (Exception ex) {
			log.error(null, ex);
		}
		return Page.EMPTY;
	}

}
