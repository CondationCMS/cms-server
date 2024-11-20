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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.extensions.http.HttpHandler;
import com.condation.cms.api.utils.RequestUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

		var file = db.getReadOnlyFileSystem().contentBase().resolve(uri);
		
		if (!file.exists()) {
			Response.writeError(request, response, callback, 404);
			return true;
		}
		
		List<NavNode> children = file.children().stream().map(child -> new NavNode(child.getFileName())).toList();
		
		NavNode node = new NavNode(file.getFileName(), children);
		
		response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
		Content.Sink.write(response, true, GSON.toJson(node), callback);
		
		return true;
	}
	
	private static record NavNode (String path, List<NavNode> children) {
		public NavNode (String path) {
			this(path, Collections.emptyList());
		}
	};
}
