package com.github.thmarx.cms.server.handler.extensions;

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


import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.extensions.HttpHandlerExtension;
import com.github.thmarx.cms.extensions.hooks.ServerHooks;
import com.github.thmarx.cms.extensions.http.JettyHttpHandlerWrapper;
import com.github.thmarx.cms.extensions.request.RequestExtensions;
import com.github.thmarx.cms.server.jetty.filter.RequestContextFilter;
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
public class JettyExtensionRouteHandler extends Handler.Abstract {

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var requestContext = (RequestContext) request.getAttribute(RequestContextFilter.REQUEST_CONTEXT);

		String extension = getExtensionName(request);
		var method = request.getMethod();
		
		var httpExtensions = requestContext.get(ServerHooks.class).getHttpRoutes();
		Optional<HttpHandlerExtension> findHttpHandler = httpExtensions.findHttpHandler(method, extension);
		
		if (findHttpHandler.isPresent()) {
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
		}
		
		findHttpHandler = requestContext.get(RequestExtensions.class).findHttpRouteHandler(method, extension);
		if (findHttpHandler.isPresent()) {
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);

		}
		return false;
	}

	private String getExtensionName(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();

		if (!contextPath.endsWith("/")) {
			contextPath += "/";
		}

		path = path.replace(contextPath, "");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

}
