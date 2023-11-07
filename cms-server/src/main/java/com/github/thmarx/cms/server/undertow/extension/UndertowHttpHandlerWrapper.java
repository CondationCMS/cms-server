package com.github.thmarx.cms.server.undertow.extension;

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

import com.github.thmarx.cms.api.extensions.http.ExtensionHttpHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Deprecated(since = "2.5.0")
public class UndertowHttpHandlerWrapper implements HttpHandler {
	
	private final ExtensionHttpHandler handler;
	
	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		handler.execute(new UndertowRequest(exchange), new UndertowResponse(exchange));
	}
	
}
