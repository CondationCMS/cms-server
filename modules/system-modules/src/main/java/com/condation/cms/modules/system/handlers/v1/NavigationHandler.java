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
import com.condation.cms.filesystem.metadata.AbstractMetaData;
import com.condation.cms.modules.system.helpers.NodeHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class NavigationHandler implements HttpHandler {

	private final DB db;
	public static final Gson GSON = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.create();

	public NavigationHandler(DB db) {
		this.db = db;
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var uri = RequestUtil.getContentPath(request);
		uri = uri.replaceFirst("api/v1/navigation", "");
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		final ReadOnlyFile contentBase = db.getReadOnlyFileSystem().contentBase();

		var file = contentBase.resolve(uri);
		
		if (!file.exists()) {
			Response.writeError(request, response, callback, 404);
			return true;
		}
		
		var filePath = PathUtil.toRelativeFile(file, contentBase);
		
		/*if (!db.getContent().isVisible(filePath)) {
			Response.writeError(request, response, callback, 403);
			return true;
		}*/
		
		final Optional<ContentNode> contentNode = db.getContent().byUri(filePath);
		
		List<NavNode> children = new ArrayList<>();
		db.getContent().listDirectories(file, "").stream()
				.filter(child -> AbstractMetaData.isVisible(child))
				.map(child -> new NavNode(
						NodeHelper.getPath(child), 
						NodeHelper.getLinks(child, request))
				).forEach(children::add);
		
		final String nodeUri = NodeHelper.getPath(uri);
		db.getContent().listContent(file, "").stream()
				.filter(child -> AbstractMetaData.isVisible(child))
				.filter(child -> 
						!NodeHelper.getPath(child).equals(nodeUri)
				)
				.map(child -> new NavNode(
						NodeHelper.getPath(child), 
						NodeHelper.getLinks(child, request))
				).forEach(children::add);
		
		children.sort((node1, node2) -> node1.path.compareTo(node2.path));
		
		NavNode node;
		if (contentNode.isPresent()) {
			node = new NavNode(
				NodeHelper.getPath(contentNode.get()), 
				NodeHelper.getLinks(contentNode.get(), request), 
				children
			);
		} else {
			node = new NavNode(
				"/" + uri, 
				Collections.emptyMap(),
				children
			);
		}
		
		
		response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
		Content.Sink.write(response, true, GSON.toJson(node), callback);
		
		return true;
	}
	
	private static record NavNode (String path, Map<String, String> _links, List<NavNode> children) {
		public NavNode (String path, Map<String, String> _links) {
			this(path, _links, Collections.emptyList());
		}
	};
}
