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
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.services.UserService;
import com.condation.cms.modules.ui.utils.TokenUtils;
import com.google.inject.Injector;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class LoginHandler extends JettyHandler {

	private final CMSModuleContext moduleContext;
	private final RequestContext requestContext;
	
	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var queryParameters = HTTPUtil.queryParameters(request.getHttpURI().getQuery());
		
		var username = queryParameters.getOrDefault("username", List.of("")).getFirst();
		var password = queryParameters.getOrDefault("password", List.of("")).getFirst();
		
		var user = moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).login(username, password);
		if (user != null) {
			var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().ui().secret();
			var token = TokenUtils.createToken(user.id(), secret);
			
			final HttpCookie cookie = HttpCookie.from(HttpCookie.from("cms-token", token), HttpCookie.SAME_SITE_ATTRIBUTE, HttpCookie.SECURE_ATTRIBUTE);
			Response.addCookie(response, cookie);
		} else {
			
		}
		
		return true;
	}
	
}
