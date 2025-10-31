package com.condation.cms.server.handler.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import com.condation.cms.api.Constants;
import com.condation.cms.api.ServerContext;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentResponse;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.content.RedirectContentResponse;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.request.RequestContextFactory;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
@Slf4j
public class JettyContentHandler extends Handler.Abstract {

	private final ContentResolver contentResolver;
	private final RequestContextFactory requestContextFactory;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
//		var uri = request.getHttpURI().getPath();
		var uri = RequestUtil.getContentPath(request);
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);

		// handle enabled spa mode
		var spaEnabled = requestContext.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties().spaEnabled();
		var notFoundContent = "/.technical/404";
		if (spaEnabled) {
			uri = "";
			notFoundContent = "/";
		}

		try {
			Optional<ContentResponse> content = contentResolver.getContent(requestContext);
			response.setStatus(200);

			if (!content.isPresent()) {

				// try to resolve static files
				content = contentResolver.getStaticContent(uri);
				if (content.isEmpty()) {
					log.debug("content not found {}", uri);
					try (var errorContext = requestContextFactory.create(request.getContext().getContextPath(),
							notFoundContent,
							queryParameters, Optional.of(request))) {
						content = contentResolver.getErrorContent(errorContext);
						response.setStatus(404);
					}
				}

			}

			var contentResponse = content.get();
			if (contentResponse instanceof RedirectContentResponse redirectContent) {
				response.getHeaders().add(HttpHeader.LOCATION, redirectContent.location());
				response.setStatus(redirectContent.status());
				callback.succeeded();
			} else if (contentResponse instanceof DefaultContentResponse defaultContent) {
				response.getHeaders().add(HttpHeader.CONTENT_TYPE, "%s; charset=utf-8".formatted(defaultContent.contentType()));
				Content.Sink.write(response, true, defaultContent.content(), callback);
			} else {
				response.setStatus(404);
				callback.succeeded();
			}

		} catch (Exception e) {
			log.error("error handling content", e);
			response.setStatus(500);
			response.getHeaders().add(HttpHeader.CONTENT_TYPE, "text/html; charset=utf-8");

			if (ServerContext.IS_DEV) {
				var stacktrace = ExceptionUtils.getStackTrace(e);
				Content.Sink.write(response, true, "<pre>%s</pre>".formatted(stacktrace), callback);
			}
		}
		return true;
	}
}
