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

import com.github.thmarx.cms.extensions.http.Request;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class UndertowRequest implements Request {

	private final HttpServerExchange exchange;

	private String body = "";
	public boolean bodyRead = false;
	
	@Override
	public String getBody () {
		return getBody(StandardCharsets.UTF_8);
	}
	
	@Override
	public String getBody (final Charset charset) {
		if (!bodyRead) {			
			try {
				body = new String(exchange.getInputStream().readAllBytes(), charset);
				bodyRead = true;
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
		return body;
	}
	
	@Override
	public List<String> getQueryParamter(final String name) {
		if (exchange.getQueryParameters().containsKey(name)) {
			return new ArrayList<>(exchange.getQueryParameters().get(name));
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getQueryParamters() {
		return new ArrayList<>(exchange.getQueryParameters().keySet());
	}
}
