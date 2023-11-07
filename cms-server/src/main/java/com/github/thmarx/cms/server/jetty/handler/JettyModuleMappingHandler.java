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
import com.github.thmarx.modules.api.Module;
import com.github.thmarx.modules.api.ModuleManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
public class JettyModuleMappingHandler extends Handler.AbstractContainer {

	private final ModuleManager moduleManager;
	private final SiteProperties siteProperties;
	
	private final Multimap<String, Mapping> moduleMappgings = ArrayListMultimap.create();
	
	public void init () {
		siteProperties.activeModules().forEach((var moduleid) -> {
			final Module module = moduleManager
					.module(moduleid);
			if (module.provides(JettyHttpHandlerExtensionPoint.class)) {
				List<JettyHttpHandlerExtensionPoint> extensions = module.extensions(JettyHttpHandlerExtensionPoint.class);
				extensions.forEach(ext -> moduleMappgings.put(moduleid, ext.getMapping()));
			}
		});
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		try {
			String moduleId = getModuleID(request);

			var module = moduleManager.module(moduleId);
			
			if (!moduleMappgings.containsKey(moduleId)) {
				Response.writeError(request, response, callback, 404);
				return false;
			}
			
			var uri = getModuleUri(request);
			Optional<Mapping> findFirst = moduleMappgings.get(moduleId).stream().filter(mapping -> mapping.getMatchingHandler(uri).isPresent()).findFirst();

			if (findFirst.isPresent()) {
				var mapping = findFirst.get();
				var handler = mapping.getMatchingHandler(uri).get();
				if (!handler.isStarted()) {
					handler.start();
				}
				return handler.handle(request, response, callback);
			}
			
			Response.writeError(request, response, callback, 404);
			return false;
		} catch (Exception e) {
			log.error(null, e);
			callback.failed(e);
			return false;
		}

	}

	private String getModuleUri(Request request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.substring(modulePath.indexOf("/"));
		}
		return modulePath;
	}

	private String getModuleID(Request request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.split("/")[0];
		}
		return modulePath;
	}

	private String getModulePath(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();
		path = path.replace(contextPath, "");

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		return path;
	}

	@Override
	public List<Handler> getHandlers() {
		return moduleMappgings.values().stream().map(mapper -> mapper.getHandlers())
				.flatMap(List::stream)
				.toList();
	}

}
