package com.condation.cms.modules.ui.utils;

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
import com.condation.cms.api.request.RequestContext;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 *
 * @author thorstenmarx
 */
public final class CookieUtil {

	private CookieUtil() {
	}

	public static Optional<HttpCookie> getCookie (Request request, String name) {
		return Request.getCookies(request).stream().filter(cookie -> name.equals(cookie.getName())).findFirst();
	}
	
	public static void setCookie(Response response, String name, String value, Duration maxAge, RequestContext requestContext) {
		boolean isDev = requestContext.has(IsDevModeFeature.class);

		HttpCookie cookie = HttpCookie.from(name, value,
				Map.of(
						HttpCookie.SAME_SITE_ATTRIBUTE, "Strict",
						HttpCookie.HTTP_ONLY_ATTRIBUTE, "true",
						HttpCookie.PATH_ATTRIBUTE, "/",
						HttpCookie.MAX_AGE_ATTRIBUTE, String.valueOf(maxAge.toSeconds())
				));
		if (!isDev) {
			cookie = HttpCookie.from(cookie, HttpCookie.SECURE_ATTRIBUTE, "true");
		}
		Response.addCookie(response, cookie);
	}
	
	public static void removeCookie (Response response, String name, RequestContext requestContext) {
		boolean isDev = requestContext.has(IsDevModeFeature.class);

		HttpCookie cookie = HttpCookie.from(name, "",
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
	}
}
