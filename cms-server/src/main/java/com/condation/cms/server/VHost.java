package com.condation.cms.server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

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


import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.eventbus.events.InvalidateTemplateCacheEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostReloadedEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostStoppedEvent;
import com.condation.cms.api.feature.features.ContentRenderFeature;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.api.utils.SiteUtil;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.core.configuration.ConfigManagement;
import com.condation.cms.extensions.GlobalExtensions;
import com.condation.cms.extensions.hooks.GlobalHooks;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.media.MediaManager;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.media.ThemeMediaManager;
import com.condation.cms.module.DefaultRenderContentFunction;
import com.condation.cms.request.RequestContextFactory;
import com.condation.cms.server.configs.ModulesModule;
import com.condation.cms.server.configs.SiteGlobalModule;
import com.condation.cms.server.configs.SiteHandlerModule;
import com.condation.cms.server.configs.SiteModule;
import com.condation.cms.server.configs.ThemeModule;
import com.condation.cms.server.filter.CreateRequestContextFilter;
import com.condation.cms.server.filter.InitRequestContextFilter;
import com.condation.cms.server.filter.PooledRequestContextFilter;
import com.condation.cms.server.filter.RequestLoggingFilter;
import com.condation.cms.server.handler.auth.JettyAuthenticationHandler;
import com.condation.cms.server.handler.cache.CacheHandler;
import com.condation.cms.server.handler.content.JettyContentHandler;
import com.condation.cms.server.handler.content.JettyTaxonomyHandler;
import com.condation.cms.server.handler.content.JettyViewHandler;
import com.condation.cms.server.handler.extensions.JettyExtensionRouteHandler;
import com.condation.cms.server.handler.extensions.JettyHttpHandlerExtensionHandler;
import com.condation.cms.server.handler.media.JettyMediaHandler;
import com.condation.cms.server.handler.module.JettyModuleHandler;
import com.condation.cms.server.handler.module.JettyRouteHandler;
import com.condation.cms.server.handler.module.JettyRoutesHandler;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class VHost {

	private final Path hostBase;

	@Getter
	private Handler hostHandler;

	@Getter
	protected Injector injector;
	
	private final Configuration configuration = new Configuration();

	public VHost(final Path hostBase) {
		this.hostBase = hostBase;
	}

	public String id() {
		return injector.getInstance(Configuration.class).get(SiteConfiguration.class).siteProperties().id();
	}

	public void shutdown() {
		try {
			injector.getInstance(EventBus.class).syncPublish(new HostStoppedEvent(id()));
			injector.getInstance(FileDB.class).close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	public void reload() {
		log.trace("reload theme");

		try {
			injector.getInstance(ConfigManagement.class).reload();

			var theme = this.injector.getInstance(Theme.class);

			this.injector.getInstance(SiteMediaManager.class).reloadTheme(theme);

			this.injector.getInstance(ThemeMediaManager.class).reloadTheme(theme);

			ResourceHandler themeAssetsHandler = this.injector.getInstance(Key.get(ResourceHandler.class, Names.named("theme")));
			themeAssetsHandler.stop();
			themeAssetsHandler.setBaseResource(new FileFolderPathResource(theme.assetsPath()));
			themeAssetsHandler.start();

			this.injector.getInstance(TemplateEngine.class).updateTheme(theme);
			this.injector.getInstance(CMSModuleContext.class).get(ThemeFeature.class).updateTheme(theme);

			injector.getInstance(EventBus.class).syncPublish(new HostReloadedEvent(id()));
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public List<String> hostnames() {
		return injector.getInstance(SiteProperties.class).hostnames();
	}

	public void init(Path modulesPath, Injector globalInjector) throws IOException {
		this.injector = globalInjector.createChildInjector(
				new SiteGlobalModule(),
				new SiteModule(hostBase, this.configuration),
				new ModulesModule(modulesPath),
				new SiteHandlerModule(),
				new ThemeModule());

		// start configuration managment
		injector.getInstance(ConfigManagement.class).initConfiguration(configuration);
		
		final CMSModuleContext cmsModuleContext = injector.getInstance(CMSModuleContext.class);
		var moduleManager = injector.getInstance(ModuleManager.class);
		var contentResolver = injector.getInstance(ContentResolver.class);
		var requestContextFactory = injector.getInstance(RequestContextFactory.class);

		cmsModuleContext.add(
				ContentRenderFeature.class,
				new ContentRenderFeature(new DefaultRenderContentFunction(contentResolver, requestContextFactory))
		);

		moduleManager.initModules();
		List<String> activeModules = getActiveModules();
		activeModules.stream()
				.filter(module_id -> moduleManager.getModuleIds().contains(module_id))
				.forEach(module_id -> {
					try {
						log.debug("activate module {}", module_id);
						moduleManager.activateModule(module_id);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				});

		injector.getInstance(EventBus.class).register(InvalidateContentCacheEvent.class, (EventListener<InvalidateContentCacheEvent>) (InvalidateContentCacheEvent event) -> {
			log.debug("invalidate content cache");
			injector.getInstance(ContentParser.class).clearCache();
		});
		injector.getInstance(EventBus.class).register(InvalidateTemplateCacheEvent.class, (EventListener<InvalidateTemplateCacheEvent>) (InvalidateTemplateCacheEvent event) -> {
			log.debug("invalidate template cache");
			injector.getInstance(TemplateEngine.class).invalidateCache();
		});

		initSiteGlobals();
	}

	private void initSiteGlobals() throws IOException {
		var globalJs = injector.getInstance(DB.class).getReadOnlyFileSystem().resolve("site.globals.js");
		if (globalJs.exists()) {
			var context = injector.getInstance(GlobalExtensions.class);
			context.evaluate(globalJs.getContent());

			injector.getInstance(GlobalHooks.class).registerCronJob();
		}
	}

	protected List<String> getActiveModules() {
		return SiteUtil.getActiveModules(
				injector.getInstance(SiteProperties.class),
				injector.getInstance(Theme.class)
		);
	}

	public Handler buildHttpHandler() {

		Handler contentHandler = null;
		if (injector.getInstance(Configuration.class).get(SiteConfiguration.class).siteProperties().cacheContent()) {
			contentHandler = new CacheHandler(injector.getInstance(JettyContentHandler.class), injector.getInstance(CacheManager.class));
		} else {
			contentHandler = injector.getInstance(JettyContentHandler.class);
		}

		var taxonomyHandler = injector.getInstance(JettyTaxonomyHandler.class);
		var viewHandler = injector.getInstance(JettyViewHandler.class);
		var routeHandler = injector.getInstance(JettyRouteHandler.class);
		var routesHandler = injector.getInstance(JettyRoutesHandler.class);
		var extensionRouteHandler = injector.getInstance(JettyExtensionRouteHandler.class);
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);

		var defaultHandlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				routeHandler,
				routesHandler,
				extensionRouteHandler,
				viewHandler,
				taxonomyHandler,
				contentHandler
		);

		log.debug("create assets handler for site");
		ResourceHandler assetsHandler = injector.getInstance(Key.get(ResourceHandler.class, Names.named("site")));

		ResourceHandler faviconHandler = new ResourceHandler();
		faviconHandler.setDirAllowed(false);
		var assetBase = this.injector.getInstance(Key.get(Path.class, Names.named("assets")));
		faviconHandler.setBaseResource(new FileFolderPathResource(assetBase.resolve("favicon.ico")));

		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(
				PathSpec.from("/"),
				requestContextFilter(defaultHandlerSequence, injector)
		);
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/favicon.ico"), faviconHandler);

		var assetsMediaManager = this.injector.getInstance(SiteMediaManager.class);
		injector.getInstance(EventBus.class).register(ConfigurationReloadEvent.class, assetsMediaManager);
		final JettyMediaHandler mediaHandler = this.injector.getInstance(Key.get(JettyMediaHandler.class, Names.named("site")));
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);

		pathMappingsHandler.addMapping(PathSpec.from("/" + JettyModuleHandler.PATH + "/*"),
				createModuleHandler()
		);

		pathMappingsHandler.addMapping(PathSpec.from("/" + JettyHttpHandlerExtensionHandler.PATH + "/*"),
				createExtensionHandler()
		);

		ContextHandler defaultContextHandler = new ContextHandler(
				pathMappingsHandler,
				injector.getInstance(SiteProperties.class).contextPath()
		);
		defaultContextHandler.setVirtualHosts(injector.getInstance(SiteProperties.class).hostnames());

		ContextHandlerCollection contextCollection = new ContextHandlerCollection(
				defaultContextHandler
		);

		if (!injector.getInstance(Theme.class).empty()) {
			var themeContextHandler = themeContextHandler();
			themeContextHandler.setVirtualHosts(injector.getInstance(SiteProperties.class).hostnames());
			contextCollection.addHandler(themeContextHandler);
		}

		RequestLoggingFilter logContextHandler = new RequestLoggingFilter(contextCollection, injector.getInstance(SiteProperties.class));

		GzipHandler gzipHandler = new GzipHandler(logContextHandler);
		gzipHandler.setMinGzipSize(1024);
		gzipHandler.addIncludedMimeTypes("text/plain");
		gzipHandler.addIncludedMimeTypes("text/html");
		gzipHandler.addIncludedMimeTypes("text/css");
		gzipHandler.addIncludedMimeTypes("application/javascript");

		hostHandler = gzipHandler;

		return hostHandler;
	}
	
	private Handler.Wrapper createExtensionHandler() {
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);
		var extensionHandler = injector.getInstance(JettyHttpHandlerExtensionHandler.class);
		var handlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				extensionHandler
		);
		return requestContextFilter(handlerSequence, injector);
	}
	
	private Handler.Wrapper createModuleHandler() {
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);
		var modulehandler = injector.getInstance(JettyModuleHandler.class);
		var handlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				modulehandler
		);
		return requestContextFilter(handlerSequence, injector);
	}

	private Handler.Wrapper requestContextFilter(Handler handler, Injector injector) {
		var performance = injector.getInstance(Configuration.class).get(ServerConfiguration.class).serverProperties().performance();
		if (performance.pool_enabled()) {
			return new PooledRequestContextFilter(handler, injector.getInstance(RequestContextFactory.class), performance);
		}
		return new CreateRequestContextFilter(handler, injector.getInstance(RequestContextFactory.class));
	}

	private String appendContextIfNeeded(final String path) {
		var contextPath = injector.getInstance(SiteProperties.class).contextPath();

		if ("/".equals(contextPath)) {
			return path;
		}

		return contextPath + path;
	}

	private ContextHandler themeContextHandler() {
		final MediaManager themeAssetsMediaManager = this.injector.getInstance(ThemeMediaManager.class);
		injector.getInstance(EventBus.class).register(ConfigurationReloadEvent.class, themeAssetsMediaManager);
		JettyMediaHandler mediaHandler = this.injector.getInstance(Key.get(JettyMediaHandler.class, Names.named("theme")));
		ResourceHandler assetsHandler = this.injector.getInstance(Key.get(ResourceHandler.class, Names.named("theme")));

		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);

		return new ContextHandler(pathMappingsHandler, appendContextIfNeeded("/theme"));
	}
}
