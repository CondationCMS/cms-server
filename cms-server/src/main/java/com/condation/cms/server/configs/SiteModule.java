package com.condation.cms.server.configs;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.commons.jexl3.JexlBuilder;
import org.graalvm.polyglot.Engine;

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


import com.condation.cms.api.Constants;
import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.cache.ICache;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.content.RenderContentFunction;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.media.MediaService;
import com.condation.cms.api.messages.MessageSource;
import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.auth.services.AuthService;
import com.condation.cms.auth.services.UserService;
import com.condation.cms.content.ContentRenderer;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.content.DefaultContentParser;
import com.condation.cms.content.DefaultContentRenderer;
import com.condation.cms.content.TaxonomyResolver;
import com.condation.cms.content.ViewResolver;
import com.condation.cms.content.tags.TagParser;
import com.condation.cms.content.template.functions.taxonomy.TaxonomyFunction;
import com.condation.cms.core.configuration.ConfigManagement;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import com.condation.cms.core.eventbus.MessagingEventBus;
import com.condation.cms.core.messages.DefaultMessageSource;
import com.condation.cms.core.messaging.DefaultMessaging;
import com.condation.cms.core.scheduler.SiteCronJobScheduler;
import com.condation.cms.core.theme.DefaultTheme;
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.filesystem.MetaData;
import com.condation.cms.media.FileMediaService;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.module.DefaultRenderContentFunction;
import com.condation.cms.request.RequestContextFactory;
import com.condation.modules.api.ModuleManager;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class SiteModule extends AbstractModule {

	private final Path hostBase;
	private final Configuration configuration;

	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(configuration);
		bind(Messaging.class).to(DefaultMessaging.class).in(Singleton.class);
		bind(EventBus.class).to(MessagingEventBus.class).in(Singleton.class);
		bind(ContentParser.class).to(DefaultContentParser.class).in(Singleton.class);
		bind(TaxonomyFunction.class).in(Singleton.class);
		bind(TaxonomyResolver.class).in(Singleton.class);
	}
	
	@Provides
	@Singleton
	public ContentNodeMapper contentNodeMapper (DB db, ContentParser contentParser) {
		return new ContentNodeMapper(db, contentParser);
	}
	
	@Provides
	@Singleton
	public TagParser tagParser (Configuration configuration) {
		var engine = new JexlBuilder()
				.strict(true)
				.cache(512);
		
		boolean IS_DEV = configuration.get(ServerConfiguration.class).serverProperties().dev();
		
		if (IS_DEV) {
			engine.silent(false);
		} else {
			engine.silent(true);
		}
		
		return new TagParser(engine.create());
	}
	
	@Provides
	@Singleton
	public ConfigManagement configurationManagement(SiteCronJobScheduler scheduler, EventBus eventBus) throws IOException {
		ConfigManagement cm = ConfigurationFactory.create(hostBase, eventBus, scheduler);
		
		return cm;
	}
	/**
	 * must not be singleton because some site properties (theme...) are allowed to be changed
	 * 
	 * @param serverProperties
	 * @return
	 * @throws IOException 
	 */
	@Provides
	public SiteProperties siteProperties(ServerProperties serverProperties) throws IOException {
		return new ExtendedSiteProperties(ConfigurationFactory.siteConfiguration(
				serverProperties.env(), 
				hostBase));
	}

	/**
	 * This method must not be Singleton because it loads the configured theme for every request
	 * 
	 * @param siteProperties
	 * @param serverProperties
	 * @param messageSource
	 * @param cacheManager
	 * @return
	 * @throws IOException 
	 */
	@Provides
	public Theme loadTheme(
		SiteProperties siteProperties, 
		ServerProperties serverProperties, 
		MessageSource messageSource,
		CacheManager cacheManager) throws IOException {

		if (siteProperties.theme() != null) {
			Path themeFolder = serverProperties.getThemesFolder().resolve(siteProperties.theme());
			return DefaultTheme.load(themeFolder, siteProperties, messageSource, serverProperties, cacheManager);
		}

		return DefaultTheme.NO_THEME;
	}
	
	@Provides
	@Singleton
	public AuthService authService(DB db) {
		return new AuthService(db.getFileSystem().hostBase());
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
	public MessageSource messages(SiteProperties site, DB db, CacheManager cacheManager) throws IOException {
		ICache<String, String> cache = cacheManager.get("messages", new CacheManager.CacheConfig(500l, Duration.ofMinutes(5)));
		var messages = new DefaultMessageSource(site, db.getFileSystem().resolve("messages/"), cache);
		return messages;
	}

	@Provides
	@Singleton
	public DB fileDb(SiteProperties site, DefaultContentParser contentParser, Configuration configuration, EventBus eventBus) throws IOException {
		var db = new FileDB(hostBase, eventBus, (file) -> {
			try {
				ReadOnlyFile cmsFile = new NIOReadOnlyFile(file, hostBase.resolve(Constants.Folders.CONTENT));
				return contentParser.parseMeta(cmsFile);
			} catch (IOException ioe) {
				log.error(null, ioe);
				throw new RuntimeException(ioe);
			}
		}, configuration);
		if ("MEMORY".equals(site.queryIndexMode())) {
			db.init(MetaData.Type.MEMORY);
		} else {
			db.init(MetaData.Type.PERSISTENT);
		}
		return db;
	}

	@Provides
	@Singleton
	public ExtensionManager extensionManager(DB db, Configuration configuration, Engine engine) throws IOException {
		var extensionManager = new ExtensionManager(
				db, 
				configuration.get(ServerConfiguration.class).serverProperties(), 
				engine
		);

		return extensionManager;
	}

	@Provides
	@Singleton
	public SiteMediaManager siteMediaManager(DB db, @Named("assets") Path assetBase, Theme theme, Configuration configuration, EventBus eventbus) throws IOException {
		var mediaManager = new SiteMediaManager(assetBase, db.getFileSystem().resolve("temp"), theme, configuration);
		eventbus.register(ConfigurationReloadEvent.class, mediaManager);
		return mediaManager;
	}

	@Provides
	@Singleton
	public MediaService mediaService(@Named("assets") Path assetBase) throws IOException {
		return new FileMediaService(assetBase);
	}

	@Provides
	@Singleton
	public RequestContextFactory requestContextFactory(Injector injector) {
		return new RequestContextFactory(
				injector
		);
	}
	
	@Provides
	@Singleton
	public RenderContentFunction renderContentFunction (ContentResolver contentResolver, RequestContextFactory requestContextFatory) {
		return new DefaultRenderContentFunction(contentResolver, requestContextFatory);
	}

	@Provides
	@Singleton
	public ContentRenderer contentRenderer(ContentParser contentParser, Injector injector, FileDB db,
			SiteProperties siteProperties, ModuleManager moduleManager) {
		return new DefaultContentRenderer(
				contentParser,
				() -> injector.getInstance(TemplateEngine.class),
				db,
				siteProperties,
				moduleManager);
	}

	@Provides
	@Singleton
	public ContentResolver contentResolver(ContentRenderer contentRenderer,
			FileDB db) {
		return new ContentResolver(contentRenderer, db);
	}

	@Provides
	@Singleton
	public ViewResolver viewResolver(ContentRenderer contentRenderer,
			FileDB db) {
		return new ViewResolver(contentRenderer, db);
	}
	
	@Provides
	@Singleton
	public CronJobContext cronJobContext() {
		final CronJobContext cronJobContext = new CronJobContext();
		
		return cronJobContext;
	}
}
