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
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.utils.TokenUtils;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class LoginHandler extends JettyHandler {

	private final CMSModuleContext moduleContext;
	private final RequestContext requestContext;

	private final ICache<String, AtomicInteger> loginFails;

	private static final int ATTEMPTS_TO_BLOCK = 3;
	private static final int MAX_KEYS = 100;
	private static final int MAX_CONTENT_SIZE = 10000;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		if (!request.getMethod().equalsIgnoreCase("POST")) {
			return false;
		}
		if (getClientLoginAttempts(request) > ATTEMPTS_TO_BLOCK) {
			response.setStatus(403);
			response.getHeaders().add("Location", managerURL("/manager/login", requestContext));
			callback.succeeded();
			return true;
		}

		Fields form = FormFields.getFields(request, MAX_KEYS, MAX_CONTENT_SIZE);

		var username = form.getValue("username");
		var password = form.getValue("password");

		var userOpt = moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).login(UserService.Realm.of("manager-users"), username, password);
		if (userOpt.isPresent()) {
			var user = userOpt.get();
			var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().ui().secret();
			var token = TokenUtils.createToken(user.username(), secret);

			boolean isDev = requestContext.has(IsDevModeFeature.class);

			HttpCookie cookie = HttpCookie.from("cms-token", token,
					Map.of(
							HttpCookie.SAME_SITE_ATTRIBUTE, "Strict",
							HttpCookie.HTTP_ONLY_ATTRIBUTE, "true"
					));
			if (!isDev) {
				cookie = HttpCookie.from(cookie, HttpCookie.SECURE_ATTRIBUTE, "true");
			}
			Response.addCookie(response, cookie);

			response.setStatus(302);
			response.getHeaders().add("Location", managerURL("/manager/index.html", requestContext));
			callback.succeeded();
		} else {
			getClientLoginCounter(request).incrementAndGet();
			response.setStatus(302);
			response.getHeaders().add("Location", managerURL("/manager/login", requestContext));
			callback.succeeded();
		}

		return true;
	}

	private int getClientLoginAttempts (Request request) {
		return getClientLoginCounter(request).get();
	}
	
	private AtomicInteger getClientLoginCounter(Request request) {
		return loginFails.get(RequestUtil.clientAddress(request));
	}

}
