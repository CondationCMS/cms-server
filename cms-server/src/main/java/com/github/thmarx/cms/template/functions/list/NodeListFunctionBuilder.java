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

import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.template.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.utils.NodeUtil;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class NodeListFunctionBuilder extends AbstractCurrentNodeFunction {

	int page = NodeListFunction.DEFAULT_PAGE;
	int size = NodeListFunction.DEFAUTL_PAGE_SIZE;

	String from = "";

	boolean index = true;

	String sort = "title";
	boolean reverse = false;

	final NodeListFunction nodeListFunction;

	final NodeListFunction nodeListFunctionNoIndex;

	public final Comparator<MetaData.MetaNode> nameComparator = (node1, node2) -> {
		var filename1 = NodeUtil.getName(node1);
		var filename2 = NodeUtil.getName(node2);
		if (filename1.equals("index.md")) {
			return -1;
		} else if (filename2.equals("index.md")) {
			return 1;
		}
		return filename1.compareTo(filename2);
	};

	public NodeListFunctionBuilder(FileSystem fileSystem, Path currentNode, ContentParser contentParser, MarkdownRenderer markdownRenderer) {
		super(fileSystem, currentNode, contentParser, markdownRenderer);
		this.nodeListFunction = new NodeListFunction(fileSystem, currentNode, contentParser, markdownRenderer);
		this.nodeListFunctionNoIndex = new NodeListFunction(fileSystem, currentNode, contentParser, markdownRenderer, true);
	}

	public NodeListFunctionBuilder from(String from) {
		this.from = from;
		return this;
	}

	public NodeListFunctionBuilder page(int page) {
		this.page = page;
		return this;
	}
	public NodeListFunctionBuilder page(long page) {
		this.page = (int)page;
		return this;
	}
	
	public NodeListFunctionBuilder page(String page) {
		this.page = Integer.parseInt(page.trim());
		return this;
	}

	public NodeListFunctionBuilder size(int size) {
		this.size = size;
		return this;
	}

	public NodeListFunctionBuilder size(long size) {
		this.size = (int)size;
		return this;
	}
	
	public NodeListFunctionBuilder size(String size) {
		this.size = Integer.parseInt(size.trim());
		return this;
	}
	
	public NodeListFunctionBuilder index(boolean index) {
		this.index = index;
		return this;
	}

	public NodeListFunctionBuilder sort(String sort) {
		this.sort = sort;
		return this;
	}

	public NodeListFunctionBuilder reverse(boolean reverse) {
		this.reverse = reverse;
		return this;
	}

	public Page<Node> list() {
		NodeListFunction function = nodeListFunction;
		if (!index) {
			function = nodeListFunctionNoIndex;
		}

		Comparator<MetaData.MetaNode> comparator = getComparator();
		if (reverse) {
			comparator = comparator.reversed();
		}

		return function.list(from, page, size, comparator);
	}

	private Comparator<MetaData.MetaNode> getComparator() {
		if (sort == null || "name".equals("sort")) {
			return nameComparator;
		} else {

			return Comparator.comparing(new Function<MetaData.MetaNode, Object>() {
				@Override
				public Object apply(MetaData.MetaNode node) {
					return node.data().get(sort);
				}
			}, Comparator.nullsLast((key1, key2) -> {
				if (Objects.equals(key1, key2)) {
					return 0;
				}
				if (key1 == null && key2 != null) {
					return -1;
				} else if (key1 != null && key2 == null) {
					return 1;
				}
				return ((Comparable)key1).compareTo((Comparable)key2);
			})
			);
		}
	}
}
