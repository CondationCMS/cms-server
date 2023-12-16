package com.github.thmarx.cms.server.jetty.modules;

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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.PropertiesLoader;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.eventbus.EventBus;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.media.MediaService;
import com.github.thmarx.cms.api.module.CMSModuleContext;
import com.github.thmarx.cms.api.theme.Theme;
import com.github.thmarx.cms.content.DefaultContentParser;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import com.github.thmarx.cms.extensions.ExtensionManager;
import com.github.thmarx.cms.filesystem.FileDB;
import com.github.thmarx.cms.media.FileMediaService;
import com.github.thmarx.cms.media.MediaManager;
import com.github.thmarx.cms.theme.DefaultTheme;
import com.github.thmarx.modules.api.ModuleManager;
import com.github.thmarx.modules.manager.ModuleAPIClassLoader;
import com.github.thmarx.modules.manager.ModuleManagerImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class SiteModule extends AbstractModule {

	private final Path modulesPath;
	private final Path hostBase;
	private final ServerProperties serverProperties;

	@Override
	protected void configure() {
		bind(ServerProperties.class).toInstance(serverProperties);
		bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
		bind(ContentParser.class).to(DefaultContentParser.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public ModuleManager moduleManager(Injector injector, CMSModuleContext context) {
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
		return ModuleManagerImpl.builder()
				.activateModulesOnStartup(false)
				.setClassLoader(classLoader)
				.setInjector((instance) -> injector.injectMembers(instance))
				.setModulesDataPath(injector.getInstance(FileDB.class).getFileSystem().resolve("modules_data").toFile())
				.setModulesPath(modulesPath.toFile())
				.setContext(context)
				.build();
	}

	@Provides
	@Singleton
	public CMSModuleContext moduleContext(SiteProperties siteProperties, ServerProperties serverProperties, FileDB db, EventBus eventBus, Theme theme) {
		return new CMSModuleContext(
				siteProperties,
				serverProperties,
				db,
				eventBus,
				theme
		);
	}

	@Provides
	@Singleton
	public Theme loadTheme(SiteProperties siteProperties) throws IOException {

		if (siteProperties.theme() != null) {
			Path themeFolder = serverProperties.getThemesFolder().resolve(siteProperties.theme());
			return DefaultTheme.load(themeFolder);
		}

		return DefaultTheme.EMPTY;
	}

	@Provides
	@Singleton
	public SiteProperties siteProperties() throws IOException {
		var props = hostBase.resolve("site.yaml");
		return PropertiesLoader.hostProperties(props);
	}

	@Provides
	@Singleton
	@Named("assets")
	public Path assetsPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.ASSETS);
	}

	@Provides
	@Singleton
	@Named("templates")
	public Path templatesPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.TEMPLATES);
	}

	@Provides
	@Singleton
	@Named("content")
	public Path contentPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.CONTENT);
	}

	@Provides
	@Singleton
	public FileDB fileDb(DB db) throws IOException {
		return (FileDB) db;
	}

	@Provides
	@Singleton
	public DB fileDb(ContentParser contentParser, SiteProperties siteProperties, EventBus eventBus) throws IOException {
		var db = new FileDB(hostBase, eventBus, (file) -> {
			try {
				return contentParser.parseMeta(file);
			} catch (IOException ioe) {
				log.error(null, ioe);
				throw new RuntimeException(ioe);
			}
		}, siteProperties);
		db.init();
		return db;
	}

	@Provides
	@Singleton
	public ExtensionManager extensionManager(DB db, Theme theme) throws IOException {
		var extensionManager = new ExtensionManager(db, theme);
		extensionManager.init();

		return extensionManager;
	}

	@Provides
	@Singleton
	@Named("site")
	public MediaManager siteMediaManager(DB db, @Named("assets") Path assetBase, Theme theme, SiteProperties siteProperties, EventBus eventbus) throws IOException {
		var mediaManager = new MediaManager(assetBase, db.getFileSystem().resolve("temp"), theme, siteProperties);
		eventbus.register(SitePropertiesChanged.class, mediaManager);
		return mediaManager;
	}

	@Provides
	@Singleton
	public MediaService mediaService(@Named("assets") Path assetBase) throws IOException {
		return new FileMediaService(assetBase);
	}
}
