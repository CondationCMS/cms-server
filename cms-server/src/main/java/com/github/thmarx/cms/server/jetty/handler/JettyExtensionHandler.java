package com.github.thmarx.cms.server.jetty.handler;

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
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import com.github.thmarx.cms.api.request.features.IsPreviewFeature;
import com.github.thmarx.cms.extensions.HttpHandlerExtension;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.request.RequestExtensions;
import com.github.thmarx.cms.server.jetty.extension.JettyHttpHandlerWrapper;
import com.github.thmarx.cms.utils.HTTPUtil;
import java.util.Map;
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
public class JettyExtensionHandler extends Handler.Abstract {

	private final RequestContextFactory requestContextFactory;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		try (var requestContext = requestContextFactory.create(request.getHttpURI().getPath(), Map.of())) {
			
			ThreadLocalRequestContext.REQUEST_CONTEXT.set(requestContext);
			var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
			if (ServerContext.IS_DEV && queryParameters.containsKey("preview")) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(true));
			} else {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(false));
			}
			
			String extension = getExtensionName(request);
			var method = request.getMethod();
			Optional<HttpHandlerExtension> findHttpHandler = requestContext.get(RequestExtensions.class).findHttpHandler(method, extension);
			if (findHttpHandler.isEmpty()) {
				response.setStatus(404);
				callback.succeeded();
				return false;
			}
			return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
		} finally {
			
		}
		
	}

	private String getExtensionName(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();
		path = path.replace(contextPath, "");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return path;
	}

}
