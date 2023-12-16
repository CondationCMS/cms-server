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
import com.github.thmarx.cms.content.ContentRenderer;
import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.module.CMSModuleContext;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.extensions.MarkdownRendererProviderExtentionPoint;
import com.github.thmarx.cms.api.extensions.TemplateEngineProviderExtentionPoint;
import com.github.thmarx.cms.api.eventbus.EventListener;
import com.github.thmarx.cms.api.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.eventbus.events.TemplateChangedEvent;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.media.MediaService;
import com.github.thmarx.cms.api.module.features.ContentRenderFeature;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.content.ContentRenderer;
import com.github.thmarx.cms.content.TaxonomyResolver;
import com.github.thmarx.cms.filesystem.FileDB;
import com.github.thmarx.cms.module.RenderContentFunction;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.server.jetty.modules.SiteHandlerModule;
import com.github.thmarx.cms.server.jetty.modules.SiteModule;
import com.github.thmarx.cms.server.jetty.modules.ThemeHandlerModule;
import com.github.thmarx.modules.api.ModuleManager;
import com.github.thmarx.modules.manager.ModuleAPIClassLoader;
import com.github.thmarx.modules.manager.ModuleManagerImpl;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class VHost {
	
	protected ContentRenderer contentRenderer;
	protected ContentResolver contentResolver;
	protected TaxonomyResolver taxonomyResolver;
	protected TemplateEngine templateEngine;
	
	protected ModuleManager moduleManager;
	
	protected final ServerProperties serverProperties;
	
	protected RequestContextFactory requestContextFactory;
	
	private final Path hostBase;
	
	protected Injector injector;
	
	public VHost(final Path hostBase, final ServerProperties serverProperties) {
		this.hostBase = hostBase;
		this.serverProperties = serverProperties;
	}
	
	public void shutdown() {
		try {
			injector.getInstance(FileDB.class).close();
			injector.getInstance(ExtensionManager.class).close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}
	
	public void updateProperties() {
		try {
			var props = injector.getInstance(FileDB.class).getFileSystem().resolve("site.yaml");
			injector.getInstance(SiteProperties.class).update(PropertiesLoader.rawProperties(props));
			
			injector.getInstance(EventBus.class).publish(new SitePropertiesChanged());
		} catch (IOException e) {
			log.error(null, e);
		}
	}
	
	public List<String> hostnames () {
		return injector.getInstance(SiteProperties.class).hostnames();
	}
	
	public void init(Path modulesPath) throws IOException {
		this.injector = Guice.createInjector(new SiteModule(modulesPath, hostBase, serverProperties),
				new SiteHandlerModule(), new ThemeHandlerModule());
		
		
		try {
			getTemplateEngine();
		} catch (Exception e) {
			log.error(null, e);
			try {
				injector.getInstance(FileDB.class).close();
			} catch (Exception ex) {
			}
			throw e;
		}
		
		var classLoader = new ModuleAPIClassLoader(ClassLoader.getSystemClassLoader(),
				List.of(
						"org.slf4j",
						"com.github.thmarx.cms",
						"org.apache.logging",
						"org.graalvm.polyglot",
						"org.graalvm.js",
						"org.eclipse.jetty",
						"jakarta.servlet"
				));
		final CMSModuleContext cmsModuleContext = injector.getInstance(CMSModuleContext.class);
		this.moduleManager = injector.getInstance(ModuleManager.class);
				
		var contentBase = this.injector.getInstance(Key.get(Path.class, Names.named("content")));		
		var db = injector.getInstance(FileDB.class);
		
		contentRenderer = new ContentRenderer(
				injector.getInstance(ContentParser.class), 
				() -> resolveTemplateEngine(), 
				db, 
				injector.getInstance(SiteProperties.class), 
				moduleManager);
		contentResolver = new ContentResolver(contentBase, contentRenderer, db);
		taxonomyResolver = new TaxonomyResolver(contentRenderer, injector.getInstance(ContentParser.class), db);
		
		this.requestContextFactory = new RequestContextFactory(
				() -> resolveMarkdownRenderer(), 
				injector.getInstance(ExtensionManager.class), 
				injector.getInstance(Theme.class), 
				injector.getInstance(SiteProperties.class), 
				injector.getInstance(MediaService.class)
		);
		
		cmsModuleContext.add(
				ContentRenderFeature.class, 
				new ContentRenderFeature(new RenderContentFunction(() -> contentResolver, () -> requestContextFactory))
		);
		
		this.moduleManager.initModules();
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
		
		moduleManager.getModuleIds().stream()
				.filter(id -> !activeModules.contains(id))
				.forEach((module_id) -> {
					try {
						log.debug("deactivate module {}", module_id);
						moduleManager.deactivateModule(module_id);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				});
		
		injector.getInstance(EventBus.class).register(ContentChangedEvent.class, (EventListener<ContentChangedEvent>) (ContentChangedEvent event) -> {
			log.debug("invalidate content cache");
			injector.getInstance(ContentParser.class).clearCache();
		});
		injector.getInstance(EventBus.class).register(TemplateChangedEvent.class, (EventListener<TemplateChangedEvent>) (TemplateChangedEvent event) -> {
			log.debug("invalidate template cache");
			resolveTemplateEngine().invalidateCache();
		});
	}
	
	protected List<String> getActiveModules() {
		List<String> activeModules = new ArrayList<>();
		activeModules.addAll(injector.getInstance(SiteProperties.class).activeModules());
		var theme = injector.getInstance(Theme.class);
		if (!theme.empty()) {
			activeModules.addAll(theme.properties().activeModules());
		}
		return activeModules;
	}
	
	private String getTemplateEngine() {
		var engine = this.injector.getInstance(SiteProperties.class).templateEngine();
		
		var theme_engine = injector.getInstance(Theme.class).properties().templateEngine();
		if (theme_engine != null && engine != null && !theme_engine.equals(engine)) {
			throw new RuntimeException("site template engine does not match theme template engine");
		}
		
		return theme_engine != null ? theme_engine : engine;
	}
	
	protected TemplateEngine resolveTemplateEngine() {
		if (this.templateEngine == null) {
			var engine = getTemplateEngine();
			
			List<TemplateEngineProviderExtentionPoint> extensions = moduleManager.extensions(TemplateEngineProviderExtentionPoint.class);
			Optional<TemplateEngineProviderExtentionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(engine)).findFirst();
			
			if (extOpt.isPresent()) {
				this.templateEngine = extOpt.get().getTemplateEngine();
			} else {
				throw new RuntimeException("no template engine found");
			}
		}
		
		return this.templateEngine;
	}
	
	protected MarkdownRenderer resolveMarkdownRenderer() {
		var engine = this.injector.getInstance(SiteProperties.class).markdownEngine();
		
		List<MarkdownRendererProviderExtentionPoint> extensions = moduleManager.extensions(MarkdownRendererProviderExtentionPoint.class);
		Optional<MarkdownRendererProviderExtentionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(engine)).findFirst();
		
		if (extOpt.isPresent()) {
			return extOpt.get().getRenderer();
		} else {
			throw new RuntimeException("no markdown renderer found");
		}
	}
}
