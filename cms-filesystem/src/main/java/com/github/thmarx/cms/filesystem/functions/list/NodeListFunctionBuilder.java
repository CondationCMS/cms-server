package com.github.thmarx.cms.filesystem.functions.list;

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

import com.github.thmarx.cms.api.db.Page;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.filesystem.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.filesystem.utils.NodeUtil;
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

	int page = Constants.DEFAULT_PAGE;
	int size = Constants.DEFAULT_PAGE_SIZE;
	int excerptLength = Constants.DEFAULT_EXCERPT_LENGTH;

	String from = "";

	boolean index = true;

	String sort = "title";
	boolean reverse = false;
	
	String contentType = Constants.DEFAULT_CONTENT_TYPE;

	final NodeListFunction nodeListFunction;

	final NodeListFunction nodeListFunctionNoIndex;

	public final Comparator<ContentNode> nameComparator = (node1, node2) -> {
		var filename1 = NodeUtil.getName(node1);
		var filename2 = NodeUtil.getName(node2);
		if (filename1.equals("index.md")) {
			return -1;
		} else if (filename2.equals("index.md")) {
			return 1;
		}
		return filename1.compareTo(filename2);
	};

	public NodeListFunctionBuilder(DB db, Path currentNode, ContentParser contentParser, MarkdownRenderer markdownRenderer) {
		super(db, currentNode, contentParser, markdownRenderer);
		this.nodeListFunction = new NodeListFunction(db, currentNode, contentParser, markdownRenderer);
		this.nodeListFunctionNoIndex = new NodeListFunction(db, currentNode, contentParser, markdownRenderer, true);
	}

	public NodeListFunctionBuilder from(String from) {
		this.from = from;
		return this;
	}

	public NodeListFunctionBuilder contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
	public NodeListFunctionBuilder json() {
		this.contentType = "application/json";
		return this;
	}
	
	public NodeListFunctionBuilder excerpt(int length) {
		this.excerptLength = length;
		return this;
	}
	public NodeListFunctionBuilder excerpt(long length) {
		this.excerptLength = (int)length;
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

		Comparator<ContentNode> comparator = getComparator();
		if (reverse) {
			comparator = comparator.reversed();
		}

		return function.list(from, page, size, excerptLength, comparator, NodeUtil.contentTypeFiler(contentType));
	}

	private Comparator<ContentNode> getComparator() {
		if (sort == null || "name".equals("sort")) {
			return nameComparator;
		} else {

			return Comparator.comparing(new Function<ContentNode, Object>() {
				@Override
				public Object apply(ContentNode node) {
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
