package com.condation.cms.modules.ui.http;

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
import com.condation.cms.api.extensions.HttpHandler;
import com.condation.cms.api.feature.FeatureContainer;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.auth.services.Realm;
import com.condation.cms.auth.services.User;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.modules.ui.utils.TokenUtils;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author t.marx
 */
@Slf4j
public abstract class JettyHandler implements HttpHandler {

	protected String getBody(Request request) {
		try (var inputStream = Request.asInputStream(request)) {

			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception ex) {
			log.error("", ex);
		}
		return "";
	}

	public void setAuthFeature (String username, SiteRequestContext requestContext) {
		if (requestContext.has(AuthFeature.class)) {
			return;
		}
		requestContext.add(AuthFeature.class, new AuthFeature(username));
	}
	
	public Optional<User> getUser(Request request, SiteModuleContext moduleContext, SiteRequestContext requestContext) {

		try {
			
			var username = requestContext.get(AuthFeature.class).username();

			return moduleContext.get(InjectorFeature.class).injector().getInstance(UserService.class).byUsername(Realm.of("manager-users"), username);
		} catch (Exception e) {
			log.error("error getting user", e);
		}
		return Optional.empty();
	}
	
	protected String getUsername (Request request, SiteModuleContext moduleContext, SiteRequestContext requestContext) {
		var user = getUser(request, moduleContext, requestContext);
		if (user.isPresent()) {
			return user.get().username();
		}
		return "";
	}

	protected String managerBaseURL(FeatureContainer featureContainer) {
		return managerURL("/manager", featureContainer);
	}
	
	protected String managerURL(String url, FeatureContainer featureContainer) {
		return HTTPUtil.modifyUrl(url, featureContainer.get(SitePropertiesFeature.class).siteProperties());
	}
}
