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


import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.modules.ui.utils.TokenUtils;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
public class UIPreviewFilter extends Handler.Abstract {

	private final Configuration configuration;

	@Inject
	public UIPreviewFilter(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public boolean handle(final Request request, final Response rspns, final Callback clbck) throws Exception {
	
		var tokenCookie = Request.getCookies(request).stream().filter(cookie -> "cms-token".equals(cookie.getName())).findFirst();
		if (tokenCookie.isEmpty()) {
			return false;
		}
		
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		
		var token = tokenCookie.get().getValue();
		var secret = configuration.get(ServerConfiguration.class).serverProperties().ui().secret();
		if (TokenUtils.validateToken(token, secret) && queryParameters.containsKey("preview")) {
			var requestContext = (RequestContext)request.getAttribute(CreateRequestContextFilter.REQUEST_CONTEXT);
			requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(IsPreviewFeature.Type.MANAGER));
		}
		
		return false;
	}

}
