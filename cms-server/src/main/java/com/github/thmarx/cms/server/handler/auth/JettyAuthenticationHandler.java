package com.github.thmarx.cms.server.handler.auth;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.thmarx.cms.api.cache.ICache;
import com.github.thmarx.cms.api.feature.features.AuthFeature;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.utils.RequestUtil;
import com.github.thmarx.cms.auth.services.AuthService;
import com.github.thmarx.cms.auth.services.UserService;
import com.github.thmarx.cms.server.jetty.filter.RequestContextFilter;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
public class JettyAuthenticationHandler extends Handler.Abstract {

	private final AuthService authService;
	private final UserService userService;
	private final ICache<String, AtomicInteger> loginFails;

//	LoadingCache<String, AtomicInteger> loginFails = Caffeine.newBuilder()
//			.maximumSize(10_000)
//			.expireAfterWrite(Duration.ofMinutes(1))
//			.expireAfterAccess(Duration.ofMinutes(1))
//			.build(key -> new AtomicInteger(0));

	static final int ATTEMPTS_TO_BLOCK = 3;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		AtomicInteger atomicInteger = getClientLoginCounter(request);
		if (atomicInteger.get() > ATTEMPTS_TO_BLOCK) {
			response.setStatus(403);
			callback.succeeded();
			return true;
		}

		var uri = "/" + RequestUtil.getContentPath(request);

		Optional<AuthService.Auth> authOpt = authService.load();
		if (authOpt.isEmpty()) {
			return false;
		}
		Optional<AuthService.AuthPath> authPathOpt = authOpt.get().find(uri);
		if (authPathOpt.isEmpty()) {
			return false;
		}
		var authPath = authPathOpt.get();

		String authHeader = request.getHeaders().get(HttpHeader.AUTHORIZATION);
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.getDecoder().decode(st.nextToken()));

						int p = credentials.indexOf(":");
						if (p != -1) {
							String username = credentials.substring(0, p).trim();
							String password = credentials.substring(p + 1).trim();

							var userOpt = userService.login(UserService.Realm.of(authPath.getRealm()), username, password);

							if (userOpt.isEmpty()) {
								unauthorized(request, response, callback, authPath.getRealm());
								return true;
							}

							if (authPath.allowed(userOpt.get())) {
								
								var requestContext = (RequestContext) request.getAttribute(RequestContextFilter.REQUEST_CONTEXT);
								requestContext.add(AuthFeature.class, new AuthFeature(username));
								
								loginFails.invalidate(clientAddress(request));
								return false;
							}

						} else {
							unauthorized(request, response, callback, authPath.getRealm());
							return true;
						}
					} catch (UnsupportedEncodingException e) {
						throw new Error("Couldn't retrieve authentication", e);
					}
				}
			}
		}
		unauthorized(request, response, callback, authPath.getRealm());
		return true;
	}

	private void unauthorized(Request request, Response response, Callback callback, String realm) throws IOException {

		getClientLoginCounter(request).incrementAndGet();

		response.getHeaders().add("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		response.setStatus(401);
		callback.succeeded();
	}

	private String clientAddress(Request request) {
		return ((InetSocketAddress) request.getConnectionMetaData().getRemoteSocketAddress())
				.getAddress().getHostAddress();
	}

	private AtomicInteger getClientLoginCounter(Request request) {
		return loginFails.get(clientAddress(request));
	}

}
