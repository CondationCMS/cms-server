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
import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.content.ContentRenderer;
import com.github.thmarx.cms.content.ContentResolver;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.CMSModuleContext;
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.extensions.MarkdownRendererProviderExtentionPoint;
import com.github.thmarx.cms.api.extensions.TemplateEngineProviderExtentionPoint;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import com.github.thmarx.cms.api.eventbus.EventListener;
import com.github.thmarx.cms.api.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.eventbus.events.TemplateChangedEvent;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.media.MediaService;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.filesystem.FileDB;
import com.github.thmarx.cms.media.FileMediaService;
import com.github.thmarx.cms.module.RenderContentFunction;
import com.github.thmarx.cms.request.RequestContextFactory;
import com.github.thmarx.cms.theme.DefaultTheme;
import com.github.thmarx.modules.api.ModuleManager;
import com.github.thmarx.modules.manager.ModuleAPIClassLoader;
import com.github.thmarx.modules.manager.ModuleManagerImpl;
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

	protected DB db;

	protected ContentRenderer contentRenderer;
	protected ContentResolver contentResolver;
	protected ContentParser contentParser;
	protected TemplateEngine templateEngine;
	protected ExtensionManager extensionManager;

	protected Path contentBase;
	protected Path assetBase;
	protected Path templateBase;

	@Getter
	private List<String> hostnames;

	@Getter
	private Theme theme;

	@Getter
	private final EventBus eventBus;

	protected SiteProperties siteProperties;

	protected ModuleManager moduleManager;

	protected final ServerProperties serverProperties;

	protected RequestContextFactory requestContextFactory;

	private final Path hostBase;

	public VHost(final Path hostBase, final ServerProperties serverProperties) {
		this.hostBase = hostBase;
		this.eventBus = new DefaultEventBus();
		this.serverProperties = serverProperties;
	}

	public void shutdown() {
		try {
			db.close();
			extensionManager.close();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	private Theme loadTheme() throws IOException {

		if (siteProperties.theme() != null) {
			Path themeFolder = serverProperties.getThemesFolder().resolve(siteProperties.theme());
			return DefaultTheme.load(themeFolder);
		}

		return DefaultTheme.EMPTY;
	}

	public void updateProperties() {
		try {
			var props = db.getFileSystem().resolve("site.yaml");
			siteProperties.update(PropertiesLoader.rawProperties(props));
			
			eventBus.publish(new SitePropertiesChanged());
		} catch (IOException e) {
			log.error(null, e);
		}
	}

	public void init(Path modules) throws IOException {

		contentParser = new ContentParser();

		this.db = new FileDB(hostBase, eventBus, (file) -> {
			try {
				return contentParser.parseMeta(file);
			} catch (IOException ioe) {
				log.error(null, ioe);
				throw new RuntimeException(ioe);
			}
		});
		((FileDB) db).init();

		var props = db.getFileSystem().resolve("site.yaml");
		siteProperties = PropertiesLoader.hostProperties(props);

		theme = loadTheme();

		try {
			getTemplateEngine();
		} catch (Exception e) {
			log.error(null, e);
			try {
				db.close();
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

		this.moduleManager = ModuleManagerImpl.create(modules.toFile(),
				db.getFileSystem().resolve("modules_data").toFile(),
				new CMSModuleContext(siteProperties, serverProperties, db, eventBus,
						new RenderContentFunction(() -> contentResolver, () -> requestContextFactory),
						theme
				),
				classLoader
		);

		hostnames = siteProperties.hostnames();

		contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);
		assetBase = db.getFileSystem().resolve(Constants.Folders.ASSETS);
		templateBase = db.getFileSystem().resolve(Constants.Folders.TEMPLATES);

		extensionManager = new ExtensionManager(db, theme);
		extensionManager.init();

		contentRenderer = new ContentRenderer(contentParser, () -> resolveTemplateEngine(), db, siteProperties, () -> moduleManager);
		contentResolver = new ContentResolver(contentBase, contentRenderer, db);

		this.requestContextFactory = new RequestContextFactory(() -> resolveMarkdownRenderer(), extensionManager, getTheme(), siteProperties, new FileMediaService(assetBase));

		this.moduleManager.initModules();

		List<String> activeModules = new ArrayList<>();
		activeModules.addAll(siteProperties.activeModules());
		if (!theme.empty()) {
			activeModules.addAll(theme.properties().activeModules());
		}

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

		eventBus.register(ContentChangedEvent.class, (EventListener<ContentChangedEvent>) (ContentChangedEvent event) -> {
			log.debug("invalidate content cache");
			contentParser.clearCache();
		});
		eventBus.register(TemplateChangedEvent.class, (EventListener<TemplateChangedEvent>) (TemplateChangedEvent event) -> {
			log.debug("invalidate template cache");
			resolveTemplateEngine().invalidateCache();
		});
	}

	private String getTemplateEngine() {
		var engine = this.siteProperties.templateEngine();

		var theme_engine = getTheme().properties().templateEngine();
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
		var engine = this.siteProperties.markdownEngine();

		List<MarkdownRendererProviderExtentionPoint> extensions = moduleManager.extensions(MarkdownRendererProviderExtentionPoint.class);
		Optional<MarkdownRendererProviderExtentionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(engine)).findFirst();

		if (extOpt.isPresent()) {
			return extOpt.get().getRenderer();
		} else {
			throw new RuntimeException("no markdown renderer found");
		}
	}
}
