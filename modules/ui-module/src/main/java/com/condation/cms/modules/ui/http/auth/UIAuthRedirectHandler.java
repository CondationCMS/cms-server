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
import com.condation.cms.api.feature.FeatureContainer;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.module.CMSRequestContext;
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
public class UIAuthRedirectHandler extends JettyHandler {

	private final CMSModuleContext moduleContext;
	private final CMSRequestContext requestContext;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		
		var tokenCookie = Request.getCookies(request).stream().filter(cookie -> "cms-token".equals(cookie.getName())).findFirst();

		if (tokenCookie.isEmpty()) {
			redirectToLogin(response, moduleContext);
			callback.succeeded();
			return true;
		}
		var token = tokenCookie.get().getValue();
		var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().ui().secret();
		if (!TokenUtils.validateToken(token, secret)) {
			redirectToLogin(response, moduleContext);
			callback.succeeded();
			return true;
		}
		var username = TokenUtils.getUserName(token, secret);
		if (username.isEmpty()) {
			redirectToLogin(response, moduleContext);
			callback.succeeded();
			return true;
		}
		setAuthFeature(username.get().username(), requestContext);

		return false;
	}

	private void redirectToLogin(Response response, FeatureContainer container) {
		response.setStatus(302);
		response.getHeaders().add("Location", managerURL("/manager/login", container));
	}

}
