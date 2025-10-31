package com.condation.cms.server.filter;

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
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.modules.ui.utils.TokenUtils;
import com.google.inject.Inject;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
public class PreviewFilter extends Handler.Abstract {

	private final Configuration configuration;

	@Inject
	public PreviewFilter(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public boolean handle(final Request request, final Response rspns, final Callback clbck) throws Exception {

		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		if (!queryParameters.containsKey("preview")) {
			return false;
		}
		var mode = IsPreviewFeature.Mode.forValue(queryParameters.get("preview").getFirst());
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);
		
		if (IsPreviewFeature.Mode.PREVIEW.equals(mode)) {
			if (ServerContext.IS_DEV) {
				requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(mode));
				return false;
			}
		}
		
		var token = handlePreviewParameter(request, rspns);

		if (token.isEmpty()) {
			token = getTokenFromCookie(request, "cms-preview-token");
		}

		if (token.isPresent() && handleToken(request, token.get())) {
			return false;
		}

		return false;
	}

	private Optional<String> getTokenFromCookie(Request request, String cookieName) {
		var tokenCookie = Request.getCookies(request).stream().filter(cookie -> cookieName.equals(cookie.getName())).findFirst();
		if (tokenCookie.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(tokenCookie.get().getValue());
	}

	private boolean handleToken(Request request, String token) {
		var secret = configuration.get(ServerConfiguration.class).serverProperties().secret();

		var payload = TokenUtils.getPayload(token, secret);

		if (payload.isPresent()) {
			var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);
			requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(IsPreviewFeature.Mode.MANAGER));

			requestContext.add(AuthFeature.class, new AuthFeature(payload.get().username()));
			return true;
		}
		return false;
	}

	private Optional<String> handlePreviewParameter(Request request, Response response) {
		var secret = configuration.get(ServerConfiguration.class).serverProperties().secret();
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		if (queryParameters.containsKey("preview-token")) {
			var token = queryParameters.get("preview-token").getFirst();
			if (TokenUtils.getPayload(token, secret).isPresent()) {
				setCookie(request, "cms-preview-token", token, response);
				return Optional.of(token);
			}
		}
		return Optional.empty();
	}

	private void setCookie(Request request, String name, String token, Response response) {

		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);

		boolean isDev = requestContext.has(IsDevModeFeature.class);

		HttpCookie cookie = HttpCookie.from(name, token,
				Map.of(
						HttpCookie.SAME_SITE_ATTRIBUTE, "Strict",
						HttpCookie.HTTP_ONLY_ATTRIBUTE, "true",
						HttpCookie.MAX_AGE_ATTRIBUTE, String.valueOf(Duration.ofHours(1).toSeconds()),
						HttpCookie.PATH_ATTRIBUTE, "/"
				));
		if (!isDev) {
			cookie = HttpCookie.from(cookie, HttpCookie.SECURE_ATTRIBUTE, "true");
		}
		Response.addCookie(response, cookie);
	}

}
