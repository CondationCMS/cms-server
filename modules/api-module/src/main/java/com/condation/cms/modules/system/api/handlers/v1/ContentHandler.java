package com.condation.cms.modules.system.api.handlers.v1;

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

import com.condation.cms.api.extensions.http.HttpHandler;
import com.condation.cms.api.utils.RequestUtil;
import static com.condation.cms.core.configuration.GSONProvider.GSON;
import com.condation.cms.modules.system.api.services.ContentService;
import java.util.Map;
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

	private final ContentService contentService;

	public ContentHandler(final ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var uri = RequestUtil.getContentPath(request);
		uri = uri.replaceFirst("api/v1/content", "");
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}

		var resolved = contentService.resolve(uri, request);
		if (resolved.isEmpty()) {
			response.setStatus(404);
			var respObject = Map.of(
					"status", 404,
					"error", "Not found"
			);
			Content.Sink.write(response, true, GSON.toJson(respObject), callback);
			return true;
		}
		
		response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/json; charset=utf-8");
		Content.Sink.write(response, true, GSON.toJson(resolved.get()), callback);
		
		return true;
	}

	
}
