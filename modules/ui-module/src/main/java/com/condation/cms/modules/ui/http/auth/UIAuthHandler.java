package com.condation.cms.modules.ui.http.auth;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class UIAuthHandler extends JettyHandler {

	private final SiteModuleContext moduleContext;
	private final SiteRequestContext requestContext;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		
		var tokenCookie = Request.getCookies(request).stream().filter(cookie -> "cms-token".equals(cookie.getName())).findFirst();

		if (tokenCookie.isEmpty()) {
			response.setStatus(403);
			callback.succeeded();
			return true;
		}
		var token = tokenCookie.get().getValue();
		var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();
		
		var payload = TokenUtils.getPayload(token, secret);

		if (payload.isEmpty()) {
			response.setStatus(403);
			callback.succeeded();
			return true;
		}

		setAuthFeature(payload.get().username(), requestContext);
		
		requestContext.add(IsPreviewFeature.class, new IsPreviewFeature(IsPreviewFeature.Mode.MANAGER));

		return false;
	}

}
