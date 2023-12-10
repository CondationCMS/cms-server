package com.github.thmarx.cms.filesystem.functions.query;

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
import com.github.thmarx.cms.api.ServerContext;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.ContentQuery;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.filesystem.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.filesystem.functions.list.Node;
import com.github.thmarx.cms.api.utils.NodeUtil;
import java.nio.file.Path;
import java.util.function.BiFunction;

/**
 *
 * @author t.marx
 */
public class QueryFunction extends AbstractCurrentNodeFunction {

	BiFunction<ContentNode, Integer, Node> nodeMapper = null;

	public QueryFunction(DB db, Path currentNode, ContentParser contentParser, MarkdownRenderer markdownRenderer) {
		super(db, currentNode, contentParser, markdownRenderer);
	}
	
	private BiFunction<ContentNode, Integer, Node> nodeMapper() {
		if (nodeMapper == null) {
			nodeMapper = (node, excerptLength) -> {
				var name = NodeUtil.getName(node);
				var temp_path = db.getFileSystem().resolve("content/").resolve(node.uri());
				var url = toUrl(node.uri());
				var md = parse(temp_path);
				var excerpt = NodeUtil.excerpt(node, md.get().content(), excerptLength, markdownRenderer);
				final Node navNode = new Node(name, url, excerpt, node.data());

				return navNode;
			};
		}

		return nodeMapper;
	}

	public ContentQuery create() {
		return db.getContent().query(nodeMapper());
	}

	public ContentQuery create(final String startUri) {
		return db.getContent().query(startUri, nodeMapper());
	}

	public String toUrl(String uri) {
		if (uri.endsWith("index.md")) {
			uri = uri.replace("index.md", "");
		}
		if (uri.endsWith(".md")) {
			uri = uri.substring(0, uri.lastIndexOf(".md"));
		}

		if (!uri.startsWith("/")) {
			uri = "/" + uri;
		}
		if (uri.length() > 1 && uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}

		return uri + (isPreview() ? "?preview" : "");
	}
}
