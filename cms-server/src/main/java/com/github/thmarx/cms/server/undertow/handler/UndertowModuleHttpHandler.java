package com.github.thmarx.cms.server.undertow.handler;

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
import com.github.thmarx.cms.server.undertow.extension.UndertowHttpHandlerWrapper;
import com.github.thmarx.modules.api.ModuleManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Deprecated(since = "2.5.0")
public class UndertowModuleHttpHandler implements HttpHandler {

	private final ModuleManager moduleManager;
	private final String method;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try {
			var moduleId = exchange.getQueryParameters().get("module").getFirst();
			
			var module = moduleManager.module(moduleId);
			
			if (module == null || !module.provides(HttpHandlerExtensionPoint.class)) {
				exchange.setStatusCode(404);
				return;
			}
			
			var extensions = module.extensions(HttpHandlerExtensionPoint.class);
			
			Optional<HttpHandlerExtensionPoint> findFirst = extensions.stream().filter(ext -> ext.handles(method, getModuleUri(exchange))).findFirst();
			
			if (findFirst.isPresent()) {
				new UndertowHttpHandlerWrapper(findFirst.get()).handleRequest(exchange);
			} else {
				exchange.setStatusCode(404);
			}
		} catch (Exception e) {
			
		}
	}
	
	private String getModuleUri (HttpServerExchange exchange) {
		var modulePath = getModulePath(exchange);
		if (modulePath.contains("/")) {
			return modulePath.substring(modulePath.indexOf("/")+1);
		}
		return modulePath;
	}
	
	private String getModulePath(HttpServerExchange exchange) {
		var path = exchange.getRequestPath();
		path = path.replace("/module/", "");
		return path;
	}

}
