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
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.ContentQuery;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.mapper.ContentNodeMapper;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.filesystem.functions.AbstractCurrentNodeFunction;
import com.github.thmarx.cms.api.model.ListNode;
import com.github.thmarx.cms.api.request.RequestContext;
import com.google.common.base.Strings;
import java.nio.file.Path;
import java.util.function.BiFunction;
import lombok.Setter;

/**
 *
 * @author t.marx
 */
public class QueryFunction extends AbstractCurrentNodeFunction {

	BiFunction<ContentNode, Integer, ListNode> nodeMapper = null;
	
	@Setter
	private String contentType;

	public QueryFunction(DB db, Path currentNode, RequestContext context) {
		super(
				db, 
				currentNode, 
				context.get(ContentParser.class), 
				context.get(MarkdownRenderer.class), 
				context.get(ContentNodeMapper.class),
				context);
	}
	
	private BiFunction<ContentNode, Integer, ListNode> nodeMapper() {
		if (nodeMapper == null) {
			nodeMapper = (node, excerptLength) -> {
				return context.get(ContentNodeMapper.class).toListNode(node, context, excerptLength);
			};
		}

		return nodeMapper;
	}

	public ContentQuery create() {
		
		var query = db.getContent().query(nodeMapper());
		
		if (!Strings.isNullOrEmpty(contentType)) {
			query.contentType(contentType);
		}
		return query;
	}

	public ContentQuery create(final String startUri) {
		var query = db.getContent().query(startUri, nodeMapper());
		if (!Strings.isNullOrEmpty(contentType)) {
			query.contentType(contentType);
		}
		return query;
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
