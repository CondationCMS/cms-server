package com.github.thmarx.cms.server.jetty.handler;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.Mapping;
import com.github.thmarx.cms.api.extensions.ServletExtensionPoint;
import com.github.thmarx.cms.api.extensions.ServletMapping;
import com.github.thmarx.modules.api.Module;
import com.github.thmarx.modules.api.ModuleManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
public class JettyServletModuleMappingHandler extends HttpServlet {

	private final ModuleManager moduleManager;
	private final SiteProperties siteProperties;
	
	private final Multimap<String, ServletMapping> moduleMappgings = ArrayListMultimap.create();
	
	@Override
	public void init () {
		siteProperties.activeModules().forEach((var moduleid) -> {
			final Module module = moduleManager
					.module(moduleid);
			if (module.provides(ServletExtensionPoint.class)) {
				List<ServletExtensionPoint> extensions = module.extensions(ServletExtensionPoint.class);
				extensions.forEach(ext -> moduleMappgings.put(moduleid, ext.getMapping()));
			}
		});
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String moduleId = getModuleID(request);

			if (!moduleMappgings.containsKey(moduleId)) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			var uri = getModuleUri(request);
			Optional<ServletMapping> findFirst = moduleMappgings.get(moduleId).stream().filter(mapping -> mapping.getMatchingServlet(uri).isPresent()).findFirst();

			if (!findFirst.isPresent()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			var mapping = findFirst.get();
			var servlet = mapping.getMatchingServlet(uri).get();
			servlet.service(request, response);
			
		} catch (Exception e) {
			log.error(null, e);
		}

	}

	private String getModuleUri(final HttpServletRequest request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.substring(modulePath.indexOf("/"));
		}
		return modulePath;
	}

	private String getModuleID(final HttpServletRequest request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.split("/")[0];
		}
		return modulePath;
	}

	private String getModulePath(final HttpServletRequest request) {
		var path = request.getRequestURI();
		var contextPath = request.getContextPath();
		path = path.replace(contextPath, "");

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		return path;
	}
}
