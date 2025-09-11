package com.condation.cms.server.handler.extensions;

/*-
 * #%L
 * cms-server
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


import com.condation.cms.api.Constants;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.extensions.HttpHandlerExtension;
import com.condation.cms.extensions.hooks.ServerHooks;
import com.condation.cms.extensions.http.JettyHttpHandlerWrapper;
import com.condation.cms.server.filter.CreateRequestContextFilter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyHttpHandlerExtensionHandler extends Handler.Abstract {

	public static final String PATH = "extension";

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);

		String extension = getExtensionName(request);
		var method = request.getMethod();
		
		var httpExtensions = requestContext.get(ServerHooks.class).getHttpExtensions();
		
		Optional<HttpHandlerExtension> findHttpHandler = httpExtensions.findHttpHandler(method, extension);
		
		if (findHttpHandler.isPresent()) {
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
		}
		
		// no extension found
		response.setStatus(404);
		callback.succeeded();
		return false;
	}

	private String getExtensionName(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = RequestUtil.getContentPath(request);

		if (!contextPath.endsWith("/")) {
			contextPath += "/";
		}
		contextPath = contextPath + PATH + "/";

		path = path.replaceFirst(contextPath, "");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

}
