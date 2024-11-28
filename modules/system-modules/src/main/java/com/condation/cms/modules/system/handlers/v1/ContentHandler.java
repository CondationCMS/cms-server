package com.condation.cms.modules.system.handlers.v1;

/*-
 * #%L
 * cms-system-modules
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.extensions.http.HttpHandler;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.modules.system.helpers.NodeHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author thmar
 */
public class ContentHandler implements HttpHandler {

	private final DB db;
	public static final Gson GSON = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.create();

	public ContentHandler(DB db) {
		this.db = db;
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var uri = RequestUtil.getContentPath(request);
		uri = uri.replaceFirst("api/v1/content", "");
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}

		var resolved = resolveContentNode(uri);
		if (resolved.isEmpty()) {
			Response.writeError(request, response, callback, 404);
			return true;
		}
		
		final ContentNode node = resolved.get();
		final Map<String, Object> data = new HashMap<>(node.data());
		data.put("_links", NodeHelper.getLinks(node, request));
		
		response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
		Content.Sink.write(response, true, GSON.toJson(data), callback);
		
		return true;
	}

	private Optional<ContentNode> resolveContentNode(String uri) {
		var contentBase = db.getReadOnlyFileSystem().contentBase();
		var contentPath = contentBase.resolve(uri);
		ReadOnlyFile contentFile = null;
		if (contentPath.exists() && contentPath.isDirectory()) {
			// use index.md
			var tempFile = contentPath.resolve("index.md");
			if (tempFile.exists()) {
				contentFile = tempFile;
			} else {
				return Optional.empty();
			}
		} else {
			var temp = contentBase.resolve(uri + ".md");
			if (temp.exists()) {
				contentFile = temp;
			} else {
				return Optional.empty();
			}
		}
		
		var filePath = PathUtil.toRelativeFile(contentFile, contentBase);
		if (!db.getContent().isVisible(filePath)) {
			return Optional.empty();
		}
		
		final ContentNode contentNode = db.getContent().byUri(filePath).get();

		return Optional.ofNullable(contentNode);
	}
}
