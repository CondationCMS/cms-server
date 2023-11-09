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
import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.*;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
@Deprecated(since = "2.5.0")
public class UndertowDefaultHttpHandler implements HttpHandler {

	private final ContentResolver contentResolver;
	private final ExtensionManager manager;

	private final Function<Context, MarkdownRenderer> markdownRendererProvider;

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		var queryParameters = new HashMap<String, List<String>>();
		exchange.getQueryParameters().entrySet().forEach(entry -> {
			queryParameters.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		});
		try (var contextHolder = manager.newContext()) {
			RequestContext context = new RequestContext(exchange.getRelativePath(), queryParameters,
					new RenderContext(contextHolder, markdownRendererProvider.apply(contextHolder.getContext()), null));
			Optional<String> content = contentResolver.getContent(context);
			if (!content.isPresent()) {
				context = new RequestContext("/.technical/404", queryParameters,
						new RenderContext(contextHolder, markdownRendererProvider.apply(contextHolder.getContext()), null));
				content = contentResolver.getContent(context);
				exchange.setStatusCode(404);
			}
			exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "text/html; charset=utf-8");
			exchange.getResponseSender().send(content.get(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("", e);
			exchange.setStatusCode(500);
			exchange.getResponseHeaders().add(HttpString.tryFromString("Content-Type"), "text/html; charset=utf-8");
		}
	}

}
