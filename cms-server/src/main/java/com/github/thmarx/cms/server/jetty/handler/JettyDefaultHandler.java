package com.github.thmarx.cms.server.jetty.handler;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
				try (var errorContext = requestContextFactory.create("/.technical/404", queryParameters)) {
					content = contentResolver.getContent(errorContext);
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
