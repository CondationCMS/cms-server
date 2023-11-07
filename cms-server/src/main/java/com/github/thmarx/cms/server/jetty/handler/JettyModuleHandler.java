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

import com.github.thmarx.cms.api.extensions.HttpHandlerExtensionPoint;
import com.github.thmarx.cms.server.jetty.extension.JettyHttpHandlerWrapper;
import com.github.thmarx.modules.api.ModuleManager;
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
@Deprecated
public class JettyModuleHandler extends Handler.Abstract {

	private final ModuleManager moduleManager;


	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		try {
			String moduleId = getModuleID(request);
			var method = request.getMethod();
			
			var module = moduleManager.module(moduleId);
			
			if (module == null || !module.provides(HttpHandlerExtensionPoint.class)) {
				response.setStatus(404);
				callback.succeeded();
				return false;
			}
			
			var extensions = module.extensions(HttpHandlerExtensionPoint.class);
			
			Optional<HttpHandlerExtensionPoint> findFirst = extensions.stream().filter(ext -> ext.handles(method, getModuleUri(request))).findFirst();
			
			if (findFirst.isPresent()) {
				return new JettyHttpHandlerWrapper(findFirst.get()).handle(request, response, callback);
			} else {
				response.setStatus(404);
				callback.succeeded();
				return false;
			}
		} catch (Exception e) {
			log.error(null, e);
			callback.failed(e);
			return false;
		}
		
	}

	private String getModuleUri (Request request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.substring(modulePath.indexOf("/")+1);
		}
		return modulePath;
	}
	
	private String getModuleID (Request request) {
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

}
