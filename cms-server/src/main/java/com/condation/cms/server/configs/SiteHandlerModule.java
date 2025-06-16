package com.condation.cms.server.configs;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.server.handler.ResourceHandler;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.api.utils.SiteUtil;
import com.condation.cms.auth.services.AuthService;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.server.FileFolderPathResource;
import com.condation.cms.server.filter.InitRequestContextFilter;
import com.condation.cms.server.filter.UIPreviewFilter;
import com.condation.cms.server.handler.auth.JettyAuthenticationHandler;
import com.condation.cms.server.handler.content.JettyContentHandler;
import com.condation.cms.server.handler.content.JettyTaxonomyHandler;
import com.condation.cms.server.handler.content.JettyViewHandler;
import com.condation.cms.server.handler.extensions.JettyHttpHandlerExtensionHandler;
import com.condation.cms.server.handler.http.APIHandler;
import com.condation.cms.server.handler.http.RoutesHandler;
import com.condation.cms.server.handler.media.JettyMediaHandler;
import com.condation.cms.server.handler.module.JettyModuleHandler;
import com.condation.cms.server.handler.module.JettyRouteHandler;
import com.condation.modules.api.ModuleManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class SiteHandlerModule extends AbstractModule {

	@Override
	protected void configure() {
		
		bind(JettyViewHandler.class).in(Singleton.class);
		bind(JettyContentHandler.class).in(Singleton.class);
		bind(JettyTaxonomyHandler.class).in(Singleton.class);
		bind(JettyRouteHandler.class).in(Singleton.class);
		bind(RoutesHandler.class).in(Singleton.class);
		bind(JettyHttpHandlerExtensionHandler.class).in(Singleton.class);
		bind(InitRequestContextFilter.class).in(Singleton.class);

		bind(APIHandler.class).in(Singleton.class);
		
		bind(UIPreviewFilter.class).in(Singleton.class);
		
		//bind(JettyAuthenticationHandler.class).in(Singleton.class);
	}
	
	@Provides
	@Singleton
	public JettyAuthenticationHandler authHandler(CacheManager cacheManager, UserService userSerivce, AuthService authService) throws IOException {
		
		ICache<String, AtomicInteger> cache = cacheManager.get("loginFails", 
				new CacheManager.CacheConfig(10_000l, Duration.ofMinutes(1)), 
				key -> new AtomicInteger(0)
		);
		
		return new JettyAuthenticationHandler(authService, userSerivce, cache);
	}
	
	@Provides
	@Singleton
	public JettyModuleHandler moduleHandler(Theme theme, ModuleManager moduleManager, SiteProperties siteProperties) throws IOException {
		return new JettyModuleHandler(moduleManager, SiteUtil.getActiveModules(siteProperties, theme));
	}
	
	@Provides
	@Singleton
	@Named("site")
	public JettyMediaHandler mediaHandler(SiteMediaManager mediaManager) throws IOException {
		return new JettyMediaHandler(mediaManager);
	}

	@Provides
	@Singleton
	@Named("site")
	public ResourceHandler resourceHander (@Named("assets") Path assetBase, ServerProperties serverProperties) throws IOException {
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(assetBase));
		if (serverProperties.dev()) {
			assetsHandler.setCacheControl("no-cache");
		} else {
			assetsHandler.setCacheControl("max-age=" + TimeUnit.HOURS.toSeconds(24));
		}
		
		return assetsHandler;
	}
}
