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
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.feature.features.CacheManagerFeature;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.User;
import com.condation.cms.auth.services.UserService;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public final class AuthUtil {

	private AuthUtil() {
	}

	private static boolean tryRefresh(Request request, Response response, SiteModuleContext moduleContext, SiteRequestContext requestContext) {
		var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();

		var refreshTokenCache = moduleContext.get(CacheManagerFeature.class).cacheManager().get(
				"refresh-tokens",
				new CacheManager.CacheConfig(1000l, Duration.ofDays(7))
		);

		var refreshCookie = CookieUtil.getCookie(request, UIConstants.COOKIE_CMS_REFRESH_TOKEN);
		if (refreshCookie.isEmpty()) {
			return false;
		}

		var token = refreshCookie.get().getValue();

		var payload = TokenUtils.getPayload(token, secret);
		if (payload.isPresent()) {
			if (refreshTokenCache.contains(token)) {
				refreshTokenCache.invalidate(token);

				Optional<User> userOpt = moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).byUsername(Realm.of("manager-users"), payload.get().username());
				if (userOpt.isPresent()) {
					updateCookies(userOpt.get(), response, requestContext, moduleContext);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean checkAuthTokens(Request request, Response response, SiteModuleContext moduleContext, SiteRequestContext requestContext) {
		var authCookie = CookieUtil.getCookie(request, UIConstants.COOKIE_CMS_TOKEN);

		var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();

		if (authCookie.isEmpty()) {
			// try refresh
			if (tryRefresh(request, response, moduleContext, requestContext)) {
				return true;
			}
		} else {
			var token = authCookie.get().getValue();

			var payload = TokenUtils.getPayload(token, secret);

			if (payload.isEmpty()) {
				// try refresh
				if (tryRefresh(request, response, moduleContext, requestContext)) {
					return true;
				}
			} else {
				return payload.get().isAuthToken();
			}
		}

		return false;
	}

	public static void updateCookies(User user, Response response, RequestContext requestContext, SiteModuleContext moduleContext) {

		try {
			var secret = moduleContext.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();
			var authToken = TokenUtils.createToken(
					user.username(),
					secret,
					new HashMap<>(Map.of("type", "auth")),
					Duration.ofMinutes(15), // Idle
					Duration.ofHours(8) // Max absolut
			);
			var refreshToken = TokenUtils.createToken(
					user.username(),
					secret,
					Duration.ofHours(1), // Idle
					Duration.ofDays(7) // Max absolut
			);

			var previewToken = TokenUtils.createToken(
					user.username(),
					secret,
					Duration.ofHours(1), // Idle
					Duration.ofHours(24) // Max absolut
			);

			CookieUtil.setCookie(response, UIConstants.COOKIE_CMS_TOKEN, authToken, Duration.ofMinutes(15), requestContext);
			CookieUtil.setCookie(response, UIConstants.COOKIE_CMS_REFRESH_TOKEN, refreshToken, Duration.ofHours(1), requestContext);
			CookieUtil.setCookie(response, UIConstants.COOKIE_CMS_PREVIEW_TOKEN, previewToken, Duration.ofHours(1), requestContext);

			var refreshTokenCache = moduleContext.get(CacheManagerFeature.class).cacheManager().get(
					"refresh-tokens",
					new CacheManager.CacheConfig(1000l, Duration.ofDays(7))
			);
			refreshTokenCache.put(refreshToken, true);
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	public static void logout(Request request, Response response,
			RequestContext requestContext, SiteModuleContext moduleContext) {

		try {
			// Entferne Token aus Cache
			var refreshCookie = CookieUtil.getCookie(request, UIConstants.COOKIE_CMS_REFRESH_TOKEN);
			if (refreshCookie.isPresent()) {
				var refreshTokenCache = moduleContext.get(CacheManagerFeature.class)
						.cacheManager()
						.get("refresh-tokens",
								new CacheManager.CacheConfig(1000l, Duration.ofDays(7)));

				refreshTokenCache.invalidate(refreshCookie.get().getValue());
			}

			// Lösche Cookies
			CookieUtil.removeCookie(response, UIConstants.COOKIE_CMS_TOKEN, requestContext);
			CookieUtil.removeCookie(response, UIConstants.COOKIE_CMS_REFRESH_TOKEN, requestContext);
			CookieUtil.removeCookie(response, UIConstants.COOKIE_CMS_PREVIEW_TOKEN, requestContext);
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}
}
