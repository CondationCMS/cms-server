package com.github.thmarx.cms.api.mapper;

/*-
 * #%L
 * cms-api
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
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.featured.Feature;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.model.ListNode;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.utils.NodeUtil;
import com.github.thmarx.cms.api.utils.PathUtil;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
public class ContentNodeMapper implements Feature {

	private final DB db;
	private final ContentParser contentParser;
	

	public ListNode toListNode(final ContentNode node, final RequestContext context) throws IOException {
		var name = NodeUtil.getName(node);
		final Path contentBase = db.getFileSystem().resolve("content/");
		var temp_path = contentBase.resolve(node.uri());
		var url = PathUtil.toURI(temp_path, contentBase);
		var md = contentParser.parse(temp_path);
		var excerpt = NodeUtil.excerpt(node, md.content(), Constants.DEFAULT_EXCERPT_LENGTH, context.get(MarkdownRenderer.class));
		return new ListNode(name, url, excerpt, node.data());
	}
}
