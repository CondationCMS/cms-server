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
import com.condation.cms.api.feature.features.IsDevModeFeature;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.modules.ui.http.JettyHandler;
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
public class LogoutHandler extends JettyHandler {

	private final SiteRequestContext requestContext;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		boolean isDev = requestContext.has(IsDevModeFeature.class);

		HttpCookie cookie = HttpCookie.from("cms-token", "",
				Map.of(
						HttpCookie.SAME_SITE_ATTRIBUTE, "Strict",
						HttpCookie.HTTP_ONLY_ATTRIBUTE, "true",
						HttpCookie.MAX_AGE_ATTRIBUTE, "0",
						HttpCookie.PATH_ATTRIBUTE, "/"
				));
		if (!isDev) {
			cookie = HttpCookie.from(cookie, HttpCookie.SECURE_ATTRIBUTE, "true");
		}
		Response.addCookie(response, cookie);

		HttpCookie preview_cookie = HttpCookie.from("cms-preview-token", "",
				Map.of(
						HttpCookie.SAME_SITE_ATTRIBUTE, "Strict",
						HttpCookie.HTTP_ONLY_ATTRIBUTE, "true",
						HttpCookie.MAX_AGE_ATTRIBUTE, "0",
						HttpCookie.PATH_ATTRIBUTE, "/"
				));
		if (!isDev) {
			preview_cookie = HttpCookie.from(preview_cookie, HttpCookie.SECURE_ATTRIBUTE, "true");
		}
		Response.addCookie(response, preview_cookie);
		
		response.setStatus(302);
		response.getHeaders().add("Location", managerURL("/manager/login", requestContext));
		callback.succeeded();

		return true;
	}

}
