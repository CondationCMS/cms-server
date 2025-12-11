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
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.mail.MailService;
import com.condation.cms.api.mail.Message;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.User;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.utils.AuthUtil;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class AjaxLoginHandler extends JettyHandler {

	private final SiteModuleContext moduleContext;
	private final RequestContext requestContext;

	private final ICache<String, AtomicInteger> loginFails;

	private final ICache<String, Login> loginAttempts;

	private static final int ATTEMPTS_TO_BLOCK = 3;

	public static record Login(User user, String token) {

	}

	public static record Command(String command, Map<String, Object> data) {

	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		if (!request.getMethod().equalsIgnoreCase("POST")) {
			return false;
		}
		if (getClientLoginAttempts(request) > ATTEMPTS_TO_BLOCK) {
			response.setStatus(403);
			callback.succeeded();
			return true;
		}

		var command = UIGsonProvider.INSTANCE.fromJson(getBody(request), Command.class);

		if (!is2FAenabled()) {
			simpleLogin(request, response, callback, command);
			return true;
		}
		
		if ("login".equals(command.command())) {
			handleLogin(request, response, callback, command);
		} else if ("validate".equals(command.command())) {
			validate(request, response, callback, command);
		}

		return true;
	}
	
	private boolean is2FAenabled () {
		return moduleContext.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties()
				.ui().force2fa();
	}
	
	private void simpleLogin (Request request, Response response, Callback callback, Command command) throws Exception {
		var username = (String) command.data().getOrDefault("username", "<empty>");
		var password = (String) command.data().getOrDefault("password", "<empty>");
		
		Optional<User> userOpt = moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).login(Realm.of("manager-users"), username, password);
		if (userOpt.isPresent()) {
			com.condation.cms.auth.services.User user = userOpt.get();
		
			AuthUtil.updateCookies(user, response, requestContext, moduleContext);

			Content.Sink.write(
					response, 
					true, 
					UIGsonProvider.INSTANCE.toJson(Map.of("status", "ok")), 
					callback);
			
		} else {
			getClientLoginCounter(request).incrementAndGet();
			Content.Sink.write(
					response, 
					true, 
					UIGsonProvider.INSTANCE.toJson(Map.of("status", "error")), 
					callback);
		}
	}

	private void handleLogin(Request request, Response response, Callback callback, Command command) throws Exception {
		var username = (String) command.data().getOrDefault("username", "<empty>");
		var password = (String) command.data().getOrDefault("password", "<empty>");

		java.util.Optional<User> userOpt = moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).login(Realm.of("manager-users"), username, password);
		if (userOpt.isPresent()) {
			com.condation.cms.auth.services.User user = userOpt.get();

			var code = generateCode();
			var login = new Login(user, code);
			loginAttempts.put(code, login);
			sendLoginCode(user, code);

			Map<String, Object> responseData = Map.of(
					"status", "2fa_required"
			);

			Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(responseData), callback);

			callback.succeeded();
		} else {
			getClientLoginCounter(request).incrementAndGet();

			Map<String, Object> responseData = Map.of(
					"status", "error"
			);

			Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(responseData), callback);
		}
	}

	private void validate(Request request, Response response, Callback callback, Command command) throws Exception {
		var code = (String) command.data().getOrDefault("code", "<empty>");

		Optional<User> userOpt = Optional.empty();
		if (loginAttempts.contains(code)) {
			userOpt = Optional.of(loginAttempts.get(code).user());
		}

		if (userOpt.isPresent()) {
			com.condation.cms.auth.services.User user = userOpt.get();
			
			AuthUtil.updateCookies(user, response, requestContext, moduleContext);

			Map<String, Object> responseData = Map.of(
					"status", "ok"
			);

			Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(responseData), callback);
		} else {
			getClientLoginCounter(request).incrementAndGet();
			Map<String, Object> responseData = Map.of(
					"status", "error"
			);

			Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(responseData), callback);
		}
	}

	private String generateCode() {
		int code = new SecureRandom().nextInt(1_000_000);
		return String.format("%06d", code);
	}

	private void sendLoginCode(User user, String code) {

		var siteProperties = moduleContext.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		
		var mailService = moduleContext.get(InjectorFeature.class).injector().getInstance(MailService.class);
		
		var message = new Message(
				(String)siteProperties.getOrDefault("ui.2fa.mail_sender", "CondationCMS"),
				new com.condation.cms.api.mail.Message.Recipient(
						user.username(), 
						(String) user.data().getOrDefault("mail", "test@localhost.de")), 
				siteProperties.getOrDefault("ui.2fa.mail_title", "CondationCMS login code"), 
				siteProperties.getOrDefault("ui.2fa.mail_message", "your code: <code>")
								.replace("<code>", code)
								.replace("<username>", user.username())
		);
		
		mailService.sendText(message);
	}

	private int getClientLoginAttempts(Request request) {
		return getClientLoginCounter(request).get();
	}

	private AtomicInteger getClientLoginCounter(Request request) {
		return loginFails.get(RequestUtil.clientAddress(request));
	}

}
