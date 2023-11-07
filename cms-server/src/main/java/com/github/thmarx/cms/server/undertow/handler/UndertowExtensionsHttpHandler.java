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

import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.extensions.HttpHandlerExtension;
import com.github.thmarx.cms.server.undertow.extension.UndertowHttpHandlerWrapper;
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
public class UndertowExtensionsHttpHandler implements HttpHandler {

	private final ExtensionManager extensionManager;
	private final String method;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try (var context = extensionManager.newContext()) {
			var path = exchange.getQueryParameters().get("name").getFirst();
			Optional<HttpHandlerExtension> findHttpHandler = context.findHttpHandler(method, "/" + path);
			if (findHttpHandler.isEmpty()) {
				exchange.setStatusCode(404);
				return;
			}
			new UndertowHttpHandlerWrapper(findHttpHandler.get().handler()).handleRequest(exchange);

		}
	}

}
