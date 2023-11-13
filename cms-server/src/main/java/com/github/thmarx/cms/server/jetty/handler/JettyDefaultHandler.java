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
import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.utils.HTTPUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
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
public class JettyDefaultHandler extends Handler.Abstract {

	private final ContentResolver contentResolver;
	private final RequestContextFactory requestContextFactory;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var uri = request.getHttpURI().getPath();
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		try (
				var requestContext = requestContextFactory.create(uri, queryParameters)) {

			Optional<String> content = contentResolver.getContent(requestContext);
			response.setStatus(200);

			if (!content.isPresent()) {
				try (var context = requestContextFactory.create("/.technical/404", queryParameters)) {
					content = contentResolver.getContent(context);
					response.setStatus(404);
				}
			}
			response.getHeaders().add("Content-Type", "text/html; charset=utf-8");

			Content.Sink.write(response, true, content.get(), callback);
		} catch (Exception e) {
			log.error("", e);
			response.setStatus(500);
			response.getHeaders().add("Content-Type", "text/html; charset=utf-8");
		}
		return true;
	}

}
