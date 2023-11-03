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
import com.github.thmarx.cms.ContentResolver;
import com.github.thmarx.cms.RenderContext;
import com.github.thmarx.cms.RequestContext;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.google.common.base.Strings;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.graalvm.polyglot.Context;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyDefaultHandler extends Handler.Abstract {

	private final ContentResolver contentResolver;
	private final ExtensionManager manager;

	private final Function<Context, MarkdownRenderer> markdownRendererProvider;

	public static Map<String, List<String>> queryParameters(String query) {
		if (Strings.isNullOrEmpty(query)) {
			return Collections.emptyMap();
		}
		return Pattern.compile("&")
				.splitAsStream(query)
				.map(s -> Arrays.copyOf(s.split("=", 2), 2))
				.collect(Collectors.groupingBy(s -> decode(s[0]), Collectors.mapping(s -> decode(s[1]), Collectors.toList())));
	}

	private static String decode(final String encoded) {
		return Optional.ofNullable(encoded)
				.map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
				.orElse(null);
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var uri = request.getHttpURI().getPath();
		var queryParameters = queryParameters(request.getHttpURI().getQuery());
		try (
				var contextHolder = manager.newContext(); 
				final MarkdownRenderer markdownRenderer = markdownRendererProvider.apply(contextHolder.getContext());) {

			RequestContext context = new RequestContext(uri, queryParameters,
					new RenderContext(contextHolder, markdownRenderer));
			Optional<String> content = contentResolver.getContent(context);
			response.setStatus(200);
			if (!content.isPresent()) {
				context = new RequestContext("/.technical/404", queryParameters,
						new RenderContext(contextHolder, markdownRenderer));
				content = contentResolver.getContent(context);
				response.setStatus(404);
			}
			response.getHeaders().add("Content-Type", "text/html; charset=utf-8");

			Content.Sink.write(response, true, content.get(), callback);
			//response.write(true, ByteBuffer.wrap(content.get().getBytes(StandardCharsets.UTF_8)), callback);
		} catch (Exception e) {
			log.error("", e);
			response.setStatus(500);
			response.getHeaders().add("Content-Type", "text/html; charset=utf-8");
		}
		return true;
	}

}
