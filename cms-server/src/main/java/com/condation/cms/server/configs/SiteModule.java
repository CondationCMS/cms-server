package com.condation.cms.server.configs;

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
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.ConfigurationManagement;
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.content.ContentParser;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.SitePropertiesChanged;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.feature.features.MessagingFeature;
import com.condation.cms.api.feature.features.ServerPropertiesFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.feature.features.ThemeFeature;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.media.MediaService;
import com.condation.cms.core.messages.DefaultMessageSource;
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
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.filesystem.MetaData;
import com.condation.cms.media.FileMediaService;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.request.RequestContextFactory;
import com.condation.cms.content.template.functions.taxonomy.TaxonomyFunction;
import com.condation.cms.core.eventbus.MessagingEventBus;
import com.condation.cms.core.messaging.DefaultMessaging;
import com.condation.cms.core.scheduler.SiteCronJobScheduler;
import com.condation.cms.core.theme.DefaultTheme;
import com.condation.modules.api.ModuleManager;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Engine;

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
	public ConfigurationManagement configurationManagement(DB db, Configuration configuration, SiteCronJobScheduler scheduler, EventBus eventBus) throws IOException {
		ConfigurationManagement cm = new ConfigurationManagement(db, configuration, scheduler, eventBus);
		cm.init();
		return cm;
	}
	
	@Provides
	public SiteProperties siteProperties(Configuration configuration) throws IOException {
		return configuration.get(SiteConfiguration.class).siteProperties();
	}

	@Provides
	public Theme loadTheme(Configuration configuration, MessageSource messageSource) throws IOException {

		var siteProperties = configuration.get(SiteConfiguration.class).siteProperties();
		var serverProperties = configuration.get(ServerConfiguration.class).serverProperties();

		if (siteProperties.theme() != null) {
			Path themeFolder = serverProperties.getThemesFolder().resolve(siteProperties.theme());
			return DefaultTheme.load(themeFolder, siteProperties, messageSource, serverProperties);
		}

		return DefaultTheme.EMPTY;
	}

	@Provides
	@Singleton
	public UserService userService(DB db) {
		return new UserService(db.getFileSystem().hostBase());
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
	public MessageSource messages(SiteProperties site, DB db) throws IOException {
		var messages = new DefaultMessageSource(site, db.getFileSystem().resolve("messages/"));
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
		if ("PERSISTENT".equals(site.queryIndexMode())) {
			db.init(MetaData.Type.PERSISTENT);
		} else {
			db.init();
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
		eventbus.register(SitePropertiesChanged.class, mediaManager);
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
	public CronJobContext cronJobContext(SiteProperties siteProperties, ServerProperties serverProperties, FileDB db, EventBus eventBus, Theme theme,
			Configuration configuration, Messaging messaging) {
		final CronJobContext cronJobContext = new CronJobContext();
		cronJobContext.add(SitePropertiesFeature.class, new SitePropertiesFeature(siteProperties));
		cronJobContext.add(ServerPropertiesFeature.class, new ServerPropertiesFeature(serverProperties));
		cronJobContext.add(DBFeature.class, new DBFeature(db));
		cronJobContext.add(EventBusFeature.class, new EventBusFeature(eventBus));
		cronJobContext.add(ThemeFeature.class, new ThemeFeature(theme));
		cronJobContext.add(MessagingFeature.class, new MessagingFeature(messaging));
		cronJobContext.add(ConfigurationFeature.class, new ConfigurationFeature(configuration));
		
		return cronJobContext;
	}
}
