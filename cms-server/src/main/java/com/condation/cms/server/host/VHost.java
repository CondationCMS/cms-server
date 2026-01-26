package com.condation.cms.server.host;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
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
import com.condation.cms.api.eventbus.events.InvalidateMediaCache;
import com.condation.cms.api.eventbus.events.InvalidateTemplateCacheEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostReloadedEvent;
import com.condation.cms.api.eventbus.events.lifecycle.HostStoppedEvent;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.core.utils.SiteUtil;
import com.condation.cms.core.configuration.ConfigManagement;
import com.condation.cms.extensions.GlobalExtensions;
import com.condation.cms.extensions.hooks.GlobalHooks;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.media.MediaManager;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.media.ThemeMediaManager;
import com.condation.cms.request.RequestContextFactory;
import com.condation.cms.server.FileFolderPathResource;
import com.condation.cms.server.configs.SiteModulesModule;
import com.condation.cms.server.configs.SiteConfigInitializer;
import com.condation.cms.server.configs.SiteGlobalModule;
import com.condation.cms.server.configs.SiteHandlerModule;
import com.condation.cms.server.configs.SiteModule;
import com.condation.cms.server.configs.ThemeModule;
import com.condation.cms.server.filter.CreateRequestContextFilter;
import com.condation.cms.server.filter.InitRequestContextFilter;
import com.condation.cms.server.filter.PooledRequestContextFilter;
import com.condation.cms.server.filter.RequestLoggingFilter;
import com.condation.cms.server.filter.PreviewFilter;
import com.condation.cms.server.handler.auth.JettyAuthenticationHandler;
import com.condation.cms.server.handler.cache.CacheHandler;
import com.condation.cms.server.handler.content.JettyContentHandler;
import com.condation.cms.server.handler.content.JettyTaxonomyHandler;
import com.condation.cms.server.handler.content.JettyViewHandler;
import com.condation.cms.server.handler.extensions.JettyHttpHandlerExtensionHandler;
import com.condation.cms.server.handler.http.APIHandler;
import com.condation.cms.server.handler.http.RoutesHandler;
import com.condation.cms.server.handler.media.JettyMediaHandler;
import com.condation.cms.server.handler.module.JettyModuleHandler;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class VHost {

	private final String siteId;
	private final Path hostBase;

	@Getter
	private Handler hostHandler;

	@Getter
	protected Injector injector;

	private final Configuration configuration = new Configuration();

	public VHost(final String siteId, final Path hostBase, Path modulesPath, Injector globalInjector) {
		this.siteId = siteId;
		this.hostBase = hostBase;
		
		this.injector = globalInjector.createChildInjector(new SiteGlobalModule(),
				new SiteModule(siteId, hostBase, this.configuration),
				new SiteModulesModule(modulesPath),
				new SiteHandlerModule(),
				new ThemeModule());
		
		// start configuration managment
		injector.getInstance(ConfigManagement.class).initConfiguration(configuration);
		// run site initializer
		injector.getInstance(SiteConfigInitializer.class).init();
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
			this.injector.getInstance(SiteModuleContext.class).get(ThemeFeature.class).updateTheme(theme);

			injector.getInstance(EventBus.class).syncPublish(new HostReloadedEvent(id()));
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public List<String> hostnames() {
		return injector.getInstance(SiteProperties.class).hostnames();
	}
	
	public void warmup () {
		injector.getAllBindings().values().stream()
			.map(Binding::getProvider)
			.forEach(Provider::get);  // Trigger eager Load
	}

	public void init() throws IOException {
		
		var moduleManager = injector.getInstance(ModuleManager.class);
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

		Initializer initializer = new Initializer(this);
		initializer.initServices();
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
		var routesHandler = injector.getInstance(RoutesHandler.class);
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);

		var uiPreviewFilter = injector.getInstance(PreviewFilter.class);

		var defaultHandlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				uiPreviewFilter,
				routesHandler,
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
				defaultHandlerSequence
		);
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/favicon.ico"), faviconHandler);

		var assetsMediaManager = this.injector.getInstance(SiteMediaManager.class);
		injector.getInstance(EventBus.class).register(ConfigurationReloadEvent.class, assetsMediaManager);
		injector.getInstance(EventBus.class).register(InvalidateMediaCache.class, (event) -> {
			if (event.mediaPath() != null) {
				assetsMediaManager.deleteTempFile(event.mediaPath());
			} else {
				assetsMediaManager.clearTempDirectory();
			}
		});
		final JettyMediaHandler mediaHandler = this.injector.getInstance(Key.get(JettyMediaHandler.class, Names.named("site")));

		var siteMediaHandlerSequence = new Handler.Sequence(
				uiPreviewFilter,
				mediaHandler
		);
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), siteMediaHandlerSequence);

		pathMappingsHandler.addMapping(PathSpec.from("/" + JettyModuleHandler.PATH + "/*"),
				createModuleHandler()
		);

		pathMappingsHandler.addMapping(PathSpec.from("/" + JettyHttpHandlerExtensionHandler.PATH + "/*"),
				createExtensionHandler()
		);

		pathMappingsHandler.addMapping(PathSpec.from("/" + APIHandler.PATH + "/*"),
				createAPIHandler()
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

		hostHandler = logContextHandler;
		
		
		return requestContextFilter(hostHandler, injector);
	}

	private Handler createAPIHandler() {
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);
		var apiHandler = injector.getInstance(APIHandler.class);
		var handlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				apiHandler
		);
		return handlerSequence;
	}

	private Handler createExtensionHandler() {
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);
		var extensionHandler = injector.getInstance(JettyHttpHandlerExtensionHandler.class);
		var handlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				extensionHandler
		);
		return handlerSequence;
	}

	private Handler createModuleHandler() {
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);
		var initContextHandler = injector.getInstance(InitRequestContextFilter.class);
		var modulehandler = injector.getInstance(JettyModuleHandler.class);
		var handlerSequence = new Handler.Sequence(
				authHandler,
				initContextHandler,
				modulehandler
		);
		return handlerSequence;
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
