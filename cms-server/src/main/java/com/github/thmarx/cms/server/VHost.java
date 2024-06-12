package com.github.thmarx.cms.server;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.configuration.Config;
import com.github.thmarx.cms.api.module.CMSModuleContext;
import com.github.thmarx.cms.api.configuration.Configuration;
import com.github.thmarx.cms.api.configuration.configs.SiteConfiguration;
import com.github.thmarx.cms.api.configuration.configs.TaxonomyConfiguration;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.EventListener;
import com.github.thmarx.cms.api.eventbus.events.ConfigurationFileChanged;
import com.github.thmarx.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.github.thmarx.cms.api.eventbus.events.InvalidateTemplateCacheEvent;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.eventbus.events.lifecycle.HostReloadedEvent;
import com.github.thmarx.cms.api.eventbus.events.lifecycle.HostStoppedEvent;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.feature.features.ContentRenderFeature;
import com.github.thmarx.cms.api.feature.features.ThemeFeature;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.filesystem.FileDB;
import com.github.thmarx.cms.media.MediaManager;
import com.github.thmarx.cms.module.DefaultRenderContentFunction;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.server.jetty.FileFolderPathResource;
import com.github.thmarx.cms.server.jetty.filter.RequestContextFilter;
import com.github.thmarx.cms.server.jetty.filter.RequestLoggingFilter;
import com.github.thmarx.cms.server.handler.content.JettyContentHandler;
import com.github.thmarx.cms.server.handler.extensions.JettyExtensionHandler;
import com.github.thmarx.cms.server.handler.media.JettyMediaHandler;
import com.github.thmarx.cms.server.handler.module.JettyHttpHandlerHandler;
import com.github.thmarx.cms.server.handler.module.JettyRouteHandler;
import com.github.thmarx.cms.server.handler.module.JettyRoutesHandler;
import com.github.thmarx.cms.server.handler.content.JettyTaxonomyHandler;
import com.github.thmarx.cms.server.handler.content.JettyViewHandler;
import com.github.thmarx.cms.server.configs.ModulesModule;
import com.github.thmarx.cms.server.configs.SiteHandlerModule;
import com.github.thmarx.cms.server.configs.SiteModule;
import com.github.thmarx.cms.server.configs.ThemeModule;
import com.github.thmarx.cms.server.handler.auth.JettyAuthenticationHandler;
import com.github.thmarx.cms.server.handler.extensions.JettyExtensionRouteHandler;
import com.github.thmarx.cms.utils.SiteUtils;
import com.github.thmarx.modules.api.ModuleManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.PathMappingsHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 *
 * @author t.marx
 */
@Slf4j
public class VHost {

	protected final Configuration configuration;

	private final Path hostBase;

	private final ScheduledExecutorService scheduledExecutorService;

	@Getter
	private Handler hostHandler;
	
	@Getter
	protected Injector injector;

	public VHost(final Path hostBase, final Configuration configuration, final ScheduledExecutorService scheduledExecutorService) {
		this.hostBase = hostBase;
		this.configuration = configuration;
		this.scheduledExecutorService = scheduledExecutorService;
	}
	
	public String id () {
		return configuration.get(SiteConfiguration.class).siteProperties().id();
	}

	public void shutdown() {
		try {
			injector.getInstance(EventBus.class).syncPublish(new HostStoppedEvent(id()));
			injector.getInstance(FileDB.class).close();
			injector.getInstance(ExtensionManager.class).close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}
	
	public void reload () {
		log.trace("reload theme");
		
		try {
			
			reloadConfiguration(SiteConfiguration.class);
			
			var theme = this.injector.getInstance(Theme.class);
		
			var themeAssetsMediaManager = this.injector.getInstance(Key.get(MediaManager.class, Names.named("theme")));
			themeAssetsMediaManager.reloadTheme(theme);
			
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

	public void reloadConfiguration(Class<? extends Config> configToReload) {
		configuration.reload(configToReload);
		if (SiteConfiguration.class.equals(configToReload)) {
			injector.getInstance(EventBus.class).publish(new SitePropertiesChanged());
		}
	}

	public List<String> hostnames() {
		return injector.getInstance(SiteProperties.class).hostnames();
	}

	public void init(Path modulesPath) throws IOException {
		this.injector = Guice.createInjector(new SiteModule(hostBase, configuration, scheduledExecutorService),
				new ModulesModule(modulesPath), new SiteHandlerModule(), new ThemeModule());

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
		injector.getInstance(EventBus.class).register(ConfigurationFileChanged.class,
				(event) -> reloadConfiguration(event.clazz()));
	}

	protected List<String> getActiveModules() {
		return SiteUtils.getActiveModules(
				injector.getInstance(SiteProperties.class),
				injector.getInstance(Theme.class)
		);
	}

	public Handler buildHttpHandler() {

		var contentHandler = injector.getInstance(JettyContentHandler.class);
		var taxonomyHandler = injector.getInstance(JettyTaxonomyHandler.class);
		var viewHandler = injector.getInstance(JettyViewHandler.class);
		var routeHandler = injector.getInstance(JettyRouteHandler.class);
		var routesHandler = injector.getInstance(JettyRoutesHandler.class);
		var extensionRouteHandler = injector.getInstance(JettyExtensionRouteHandler.class);
		var authHandler = injector.getInstance(JettyAuthenticationHandler.class);

		var defaultHandlerSequence = new Handler.Sequence(
				authHandler,
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
				new RequestContextFilter(defaultHandlerSequence, injector.getInstance(RequestContextFactory.class))
		);
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/favicon.ico"), faviconHandler);

		var assetsMediaManager = this.injector.getInstance(Key.get(MediaManager.class, Names.named("site")));
		injector.getInstance(EventBus.class).register(SitePropertiesChanged.class, assetsMediaManager);
		final JettyMediaHandler mediaHandler = this.injector.getInstance(Key.get(JettyMediaHandler.class, Names.named("site")));
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);

		pathMappingsHandler.addMapping(PathSpec.from("/" + JettyHttpHandlerHandler.PATH + "/*"),
				new RequestContextFilter(injector.getInstance(JettyHttpHandlerHandler.class), injector.getInstance(RequestContextFactory.class))
		);

		pathMappingsHandler.addMapping(
				PathSpec.from("/" + JettyExtensionHandler.PATH + "/*"),
				new RequestContextFilter(injector.getInstance(JettyExtensionHandler.class), injector.getInstance(RequestContextFactory.class))
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

	private String appendContextIfNeeded(final String path) {
		var contextPath = injector.getInstance(SiteProperties.class).contextPath();

		if ("/".equals(contextPath)) {
			return path;
		}

		return contextPath + path;
	}

	private ContextHandler themeContextHandler() {
		final MediaManager themeAssetsMediaManager = this.injector.getInstance(Key.get(MediaManager.class, Names.named("theme")));
		injector.getInstance(EventBus.class).register(SitePropertiesChanged.class, themeAssetsMediaManager);
		JettyMediaHandler mediaHandler = this.injector.getInstance(Key.get(JettyMediaHandler.class, Names.named("theme")));
		ResourceHandler assetsHandler = this.injector.getInstance(Key.get(ResourceHandler.class, Names.named("theme")));

		PathMappingsHandler pathMappingsHandler = new PathMappingsHandler();
		pathMappingsHandler.addMapping(PathSpec.from("/assets/*"), assetsHandler);
		pathMappingsHandler.addMapping(PathSpec.from("/media/*"), mediaHandler);

		return new ContextHandler(pathMappingsHandler, appendContextIfNeeded("/theme"));
	}
}
