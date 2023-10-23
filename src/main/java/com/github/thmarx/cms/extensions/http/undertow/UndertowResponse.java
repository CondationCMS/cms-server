package com.github.thmarx.cms.extensions.http.undertow;

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

import com.github.thmarx.cms.extensions.http.Response;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.Charset;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class UndertowResponse implements Response {
	
	private final HttpServerExchange exchange;

	@Override
	public void addHeader(String name, String value) {
		exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
	}

	@Override
	public void write(String content, Charset charset) {
		exchange.getResponseSender().send(content, charset);
	}
}
