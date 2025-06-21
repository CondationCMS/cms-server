package com.condation.cms.server.handler.http;

import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.extensions.http.APIHandlerExtensionPoint;
import com.condation.cms.api.extensions.http.PathMapping;
import com.condation.cms.api.feature.features.ConfigurationFeature;

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

import com.condation.cms.api.request.RequestContext;
import com.condation.cms.extensions.HttpHandlerExtension;
import com.condation.cms.extensions.hooks.ServerHooks;
import com.condation.cms.extensions.http.JettyHttpHandlerWrapper;
import com.condation.cms.server.filter.CreateRequestContextFilter;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Inject;

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
@RequiredArgsConstructor(onConstructor = @__({
        @Inject }))
@Slf4j
public class APIHandler extends Handler.Abstract {

    public static final String PATH = "api";

    private final ModuleManager moduleManager;

	private boolean isApiActivated (Request request) {
		var requestContext = (RequestContext) request.getAttribute(CreateRequestContextFilter.REQUEST_CONTEXT);
		var siteProperties = requestContext.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		
		return siteProperties.getOrDefault("api.enabled", Boolean.FALSE);
	}
	
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
		
		if (!isApiActivated(request)) {
			return false;
		}
		
        try {
            if (handleExtensionRoute(request, response, callback)) {
                return true;
            }

            if (handleModuleRoute(request, response, callback)) {
                return true;
            }

            Response.writeError(request, response, callback, 404);
            return true;
        } catch (Exception e) {
            log.error(null, e);
			callback.failed(e);
			return true;
        }
    }

    private boolean handleModuleRoute(Request request, Response response, Callback callback) throws Exception {

        String apiRoute = getApiRoute(request);
        var method = request.getMethod();

        Optional<PathMapping> firstMatch = moduleManager.extensions(APIHandlerExtensionPoint.class)
                .stream()
                .filter(extension -> extension.getMapping().getMatchingHandler(apiRoute, method).isPresent())
                .map(extension -> extension.getMapping())
                .findFirst();

        if (firstMatch.isPresent()) {
            var mapping = firstMatch.get();
            var handler = mapping.getMatchingHandler(apiRoute, method).get();
            return handler.handle(request, response, callback);
        }

        return false;
    }

    private boolean handleExtensionRoute(Request request, Response response, Callback callback) throws Exception {
        var requestContext = (RequestContext) request.getAttribute(CreateRequestContextFilter.REQUEST_CONTEXT);

        String extension = getApiRoute(request);
        var method = request.getMethod();

        var httpExtensions = requestContext.get(ServerHooks.class).getAPIRoutes();
        Optional<HttpHandlerExtension> findHttpHandler = httpExtensions.findHttpHandler(method, extension);

        if (findHttpHandler.isPresent()) {
            return new JettyHttpHandlerWrapper(findHttpHandler.get().handler()).handle(request, response, callback);
        }

        return false;
    }

    private String getApiRoute(Request request) {
        var path = request.getHttpURI().getPath();
        var contextPath = request.getContext().getContextPath();

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