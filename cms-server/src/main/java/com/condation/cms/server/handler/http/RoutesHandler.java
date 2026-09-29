package com.condation.cms.server.handler.http;

/*-
 * #%L
 * CMS Server
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import com.condation.cms.api.extensions.HttpRoutesExtensionPoint;
import com.condation.cms.api.extensions.Mapping;
import com.condation.cms.api.extensions.http.routes.RoutesExtensionPoint;
import com.condation.cms.api.extensions.http.routes.RoutesManager;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.extensions.HttpHandlerExtension;
import com.condation.cms.extensions.http.JettyHttpHandlerWrapper;
import com.condation.cms.hooksystem.extensions.ServerHooks;
import com.condation.modules.api.ModuleManager;
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
public class RoutesHandler extends Handler.Abstract {

	private final ModuleManager moduleManager;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		try {
			if (tryExtensionRoutes(request, response, callback)) {
				return true;
			}

			if (tryModuleRoutes(request, response, callback)) {
				return true;
			}
			
            return tryRoutesManager(request, response, callback);
		} catch (Exception e) {
			log.error(null, e);
			callback.failed(e);
			return true;
		}
	}

	private boolean tryRoutesManager (Request request, Response response, Callback callback) throws Exception {
		String route = "/" + RequestUtil.getContentPath(request);
		
		RoutesManager routesManager = new RoutesManager();
		
		
		moduleManager.extensions(RoutesExtensionPoint.class)
				.forEach(routesManager::register);
		
		var handler = routesManager.findFirst(route, request.getMethod());
		if (handler.isPresent()) {
			return handler.get().handle(request, response, callback);
		}
		
		return false;
	}
	
	private boolean tryModuleRoutes(Request request, Response response, Callback callback) throws Exception {
		String route = "/" + RequestUtil.getContentPath(request);

		Optional<Mapping> firstMatch = moduleManager.extensions(HttpRoutesExtensionPoint.class)
				.stream()
				.filter(extension -> extension.getMapping().getMatchingHandler(route).isPresent())
				.map(extension -> extension.getMapping())
				.findFirst();

		if (firstMatch.isPresent()) {
			var mapping = firstMatch.get();
			var handler = mapping.getMatchingHandler(route).get();
			return handler.handle(request, response, callback);
		}

		return false;
	}

	private boolean tryExtensionRoutes(Request request, Response response, Callback callback) throws Exception {
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);

		String route = "/" + RequestUtil.getContentPath(request);
		var method = request.getMethod();

		var httpExtensions = requestContext.get(ServerHooks.class).getHttpRoutes();
		Optional<HttpHandlerExtension> findHttpHandler = httpExtensions.findHttpHandler(method, route);

		if (findHttpHandler.isPresent()) {
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
		}
		return false;
	}
}
