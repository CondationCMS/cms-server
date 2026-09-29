package com.condation.cms.server.configs;

/*-
 * #%L
 * CMS Server
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.server.handler.ResourceHandler;


import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.injector.Injector;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.content.CollectionResolver;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.content.TaxonomyResolver;
import com.condation.cms.content.ViewResolver;
import com.condation.cms.core.utils.SiteUtil;
import com.condation.cms.auth.services.AuthService;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.server.FileFolderPathResource;
import com.condation.cms.server.filter.InitRequestContextFilter;
import com.condation.cms.server.filter.PreviewFilter;
import com.condation.cms.request.RequestContextFactory;
import com.condation.cms.server.handler.StaticFileHandler;
import com.condation.cms.server.handler.WellKnownHandler;
import com.condation.cms.server.handler.auth.JettyAuthenticationHandler;
import com.condation.cms.server.handler.content.JettyCollectionHandler;
import com.condation.cms.server.handler.content.JettyContentHandler;
import com.condation.cms.server.handler.content.JettyTaxonomyHandler;
import com.condation.cms.server.handler.content.JettyViewHandler;
import com.condation.cms.server.handler.extensions.JettyHttpHandlerExtensionHandler;
import com.condation.cms.server.handler.http.APIHandler;
import com.condation.cms.server.handler.http.RoutesHandler;
import com.condation.cms.server.handler.media.JettyMediaHandler;
import com.condation.cms.server.handler.module.JettyModuleHandler;
import com.condation.modules.api.ModuleManager;
import static com.condation.cms.server.configs.ProviderSupport.provide;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class SiteHandlerModule implements com.condation.cms.api.injector.Module {

	@Override
	public void register(Injector injector) {
		injector.register(JettyViewHandler.class,
				i -> new JettyViewHandler(i.getInstance(ViewResolver.class))).singleton();
		injector.register(JettyCollectionHandler.class,
				i -> new JettyCollectionHandler(i.getInstance(CollectionResolver.class))).singleton();
		injector.register(JettyContentHandler.class,
				i -> new JettyContentHandler(i.getInstance(ContentResolver.class),
						i.getInstance(RequestContextFactory.class))).singleton();
		injector.register(JettyTaxonomyHandler.class,
				i -> new JettyTaxonomyHandler(i.getInstance(TaxonomyResolver.class))).singleton();
		injector.register(RoutesHandler.class,
				i -> new RoutesHandler(i.getInstance(ModuleManager.class))).singleton();
		injector.register(JettyHttpHandlerExtensionHandler.class,
				_ -> new JettyHttpHandlerExtensionHandler()).singleton();
		injector.register(InitRequestContextFilter.class,
				i -> new InitRequestContextFilter(i.getInstance(RequestContextFactory.class))).singleton();
		injector.register(APIHandler.class,
				i -> new APIHandler(i.getInstance(ModuleManager.class))).singleton();
		injector.register(PreviewFilter.class,
				i -> new PreviewFilter(i.getInstance(Configuration.class))).singleton();
		injector.register(JettyAuthenticationHandler.class,
				i -> provide(() -> authHandler(i.getInstance(CacheManager.class), i.getInstance(UserService.class),
						i.getInstance(AuthService.class)))).singleton();
		injector.register(JettyModuleHandler.class,
				i -> provide(() -> moduleHandler(i.getInstance(Theme.class), i.getInstance(ModuleManager.class),
						i.getInstance(SiteProperties.class)))).singleton();
		injector.register(Constants.DiScopes.SITE_MEDIA, JettyMediaHandler.class,
				i -> provide(() -> mediaHandler(i.getInstance(SiteMediaManager.class)))).singleton();
		injector.register(Constants.DiScopes.SITE_ASSETS, ResourceHandler.class,
				i -> provide(() -> assetsHandler(i.getInstance(Constants.DiScopes.ASSETS, Path.class),
						i.getInstance(ServerProperties.class)))).singleton();
		injector.register(Constants.DiScopes.SITE_PUBLIC, StaticFileHandler.class,
				i -> provide(() -> publicHandler(i.getInstance(Constants.DiScopes.PUBLIC, Path.class)))).singleton();
		injector.register(WellKnownHandler.class,
				i -> provide(() -> wellKnownHandler(i.getInstance(Constants.DiScopes.PUBLIC, Path.class),
						i.getInstance(Theme.class)))).singleton();
	}
	
	public JettyAuthenticationHandler authHandler(CacheManager cacheManager, UserService userSerivce, AuthService authService) throws IOException {
		
		ICache<String, AtomicInteger> cache = cacheManager.get("loginFails", 
				new CacheManager.CacheConfig(10_000l, Duration.ofMinutes(1)), 
				key -> new AtomicInteger(0)
		);
		
		return new JettyAuthenticationHandler(authService, userSerivce, cache);
	}
	
	public JettyModuleHandler moduleHandler(Theme theme, ModuleManager moduleManager, SiteProperties siteProperties) throws IOException {
		return new JettyModuleHandler(moduleManager, SiteUtil.getActiveModules(siteProperties, theme));
	}
	
	public JettyMediaHandler mediaHandler(SiteMediaManager mediaManager) throws IOException {
		return new JettyMediaHandler(mediaManager);
	}

	public ResourceHandler assetsHandler (Path assetBase, ServerProperties serverProperties) {
		ResourceHandler assetsHandler = new ResourceHandler();
		assetsHandler.setDirAllowed(false);
		assetsHandler.setBaseResource(new FileFolderPathResource(assetBase));
        assetsHandler.setEtags(true);
		if (serverProperties.dev()) {
			assetsHandler.setCacheControl("no-cache");
		} else {
			assetsHandler.setCacheControl(
                "public, max-age=0, must-revalidate"
        );
		}
		
		return assetsHandler;
	}
    
	public StaticFileHandler publicHandler (Path publicBase) {
		return new StaticFileHandler(List.of(publicBase));
	}
	
	public WellKnownHandler wellKnownHandler (Path publicBase, Theme theme) {
		
		List<Path> paths = new ArrayList<>();
		paths.add(publicBase);
        paths.add(theme.publicPath());
        
        if (theme.getParentTheme() != null) {
            paths.add(theme.getParentTheme().publicPath());
        }
		
		return new WellKnownHandler(List.of(publicBase));
	}
}
