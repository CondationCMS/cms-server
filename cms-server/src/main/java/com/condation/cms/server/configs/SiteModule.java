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

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.commons.jexl3.JexlBuilder;
import org.graalvm.polyglot.Engine;


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
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.injector.Injector;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.mail.MailService;
import com.condation.cms.api.menu.Menu;
import com.condation.cms.api.menu.MenuService;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.media.MediaService;
import com.condation.cms.api.messages.MessageSource;
import com.condation.cms.api.messaging.Messaging;
import com.condation.cms.api.repository.ContentRepository;
import com.condation.cms.api.repository.CollectionRepository;
import com.condation.cms.api.repository.MutableCollectionRepository;
import com.condation.cms.api.repository.MutableContentRepository;
import com.condation.cms.api.repository.ContentStore;
import com.condation.cms.api.scheduler.CronJobContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.api.workflow.WFTransition;
import com.condation.cms.api.workflow.Workflow;
import com.condation.cms.api.workflow.WorkflowInstance;
import com.condation.cms.auth.services.AuthService;
import com.condation.cms.content.CollectionResolver;
import com.condation.cms.content.ContentRenderer;
import com.condation.cms.content.ContentResolver;
import com.condation.cms.content.DefaultContentParser;
import com.condation.cms.content.DefaultContentRenderer;
import com.condation.cms.content.DefaultVariantSelector;
import com.condation.cms.content.ConfigurableVariantSelector;
import com.condation.cms.content.TaxonomyResolver;
import com.condation.cms.api.variants.VariantSelector;
import com.condation.cms.content.VariantSelectorConfigurationRepository;
import com.condation.cms.content.ViewResolver;
import com.condation.cms.content.shortcodes.ShortCodeParser;
import com.condation.cms.content.template.functions.taxonomy.TaxonomyFunction;
import com.condation.cms.core.request.visitor.VisitorContextService;
import com.condation.cms.core.configuration.ConfigManagement;
import com.condation.cms.core.configuration.ConfigurationFactory;
import com.condation.cms.core.configuration.properties.ExtendedSiteProperties;
import com.condation.cms.core.eventbus.MessagingEventBus;
import com.condation.cms.core.mail.DefaultMailService;
import com.condation.cms.core.menu.CachingMenuService;
import com.condation.cms.core.menu.FileMenuService;
import com.condation.cms.core.messages.DefaultMessageSource;
import com.condation.cms.core.messaging.DefaultMessaging;
import com.condation.cms.core.scheduler.SiteCronJobScheduler;
import com.condation.cms.core.theme.DefaultTheme;
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.filesystem.FileDB;
import com.condation.cms.filesystem.FileSystemContentRepository;
import com.condation.cms.filesystem.FileSystemContentStore;
import com.condation.cms.filesystem.NIOReadOnlyFile;
import com.condation.cms.media.FileMediaService;
import com.condation.cms.media.SiteMediaManager;
import com.condation.cms.module.DefaultRenderContentFunction;
import com.condation.cms.request.RequestContextFactory;
import com.condation.modules.api.ModuleManager;
import static com.condation.cms.server.configs.ProviderSupport.provide;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class SiteModule implements com.condation.cms.api.injector.Module {

	private final String siteId;
	private final Path hostBase;
	private final Configuration configuration;

	@Override
	public void register(Injector injector) {
		injector.register(Configuration.class, _ -> configuration).singleton();
		injector.register(EventBus.class,
				i -> new MessagingEventBus(i.getInstance(Messaging.class))).singleton();
		injector.register(TaxonomyFunction.class,
				i -> new TaxonomyFunction(i.getInstance(DB.class))).singleton();
		injector.register(TaxonomyResolver.class,
				i -> new TaxonomyResolver(i.getInstance(ContentRenderer.class), i.getInstance(DB.class),
						i.getInstance(ContentNodeMapper.class), i.getInstance(ContentRepository.class))).singleton();
		injector.register(DefaultContentParser.class, _ -> new DefaultContentParser());
		injector.register(Workflow.class, _ -> workflow()).singleton();
		injector.register(Messaging.class, _ -> messaging()).singleton();
		injector.register(ContentNodeMapper.class,
				i -> contentNodeMapper(i.getInstance(ContentRepository.class))).singleton();
		injector.register(ContentParser.class,
				i -> contentParser(i.getInstance(Configuration.class), i.getInstance(CacheManager.class))).singleton();
		injector.register(ShortCodeParser.class,
				i -> ShortCodeParser(i.getInstance(Configuration.class), i.getInstance(MarkdownRenderer.class))).singleton();
		injector.register(ConfigManagement.class,
				i -> provide(() -> configurationManagement(i.getInstance(SiteCronJobScheduler.class),
						i.getInstance(EventBus.class)))).singleton();
		injector.register(SiteProperties.class,
				i -> provide(() -> siteProperties(i.getInstance(ServerProperties.class))));
		injector.register(Theme.class,
				i -> provide(() -> loadTheme(i.getInstance(SiteProperties.class), i.getInstance(ServerProperties.class),
						i.getInstance(MessageSource.class), i.getInstance(CacheManager.class))));
		injector.register(AuthService.class, i -> authService(i.getInstance(DB.class))).singleton();
		injector.register(MenuService.class,
				i -> menuService(i.getInstance(DB.class), i.getInstance(CacheManager.class),
						i.getInstance(EventBus.class))).singleton();
		injector.register(Constants.DiScopes.ASSETS, Path.class, i -> assetsPath(i.getInstance(DB.class))).singleton();
		injector.register(Constants.DiScopes.PUBLIC, Path.class, i -> publicPath(i.getInstance(DB.class))).singleton();
		injector.register(Constants.DiScopes.TEMPLATES, Path.class, i -> templatesPath(i.getInstance(DB.class))).singleton();
		injector.register(Constants.DiScopes.CONTENT, Path.class, i -> contentPath(i.getInstance(DB.class))).singleton();
		injector.register(FileDB.class, i -> provide(() -> fileDb(i.getInstance(DB.class)))).singleton();
		injector.register(MessageSource.class,
				i -> provide(() -> messages(i.getInstance(SiteProperties.class), i.getInstance(DB.class),
						i.getInstance(CacheManager.class)))).singleton();
		injector.register(DB.class,
				i -> provide(() -> fileDb(
						i.getInstance(DefaultContentParser.class), i.getInstance(Configuration.class),
						i.getInstance(EventBus.class)))).eager();
		injector.register(ContentStore.class, i -> contentStore(i.getInstance(DB.class))).singleton();
		injector.register(MutableContentRepository.class,
				i -> mutableContentRepository(i.getInstance(DB.class), i.getInstance(ContentStore.class),
						i.getInstance(ContentParser.class))).singleton();
		injector.register(ContentRepository.class,
				i -> contentRepository(i.getInstance(MutableContentRepository.class))).singleton();
		injector.register(MutableCollectionRepository.class,
				i -> mutableCollectionRepository(i.getInstance(DB.class))).singleton();
		injector.register(CollectionRepository.class,
				i -> collectionRepository(i.getInstance(MutableCollectionRepository.class))).singleton();
		injector.register(ExtensionManager.class,
				i -> provide(() -> extensionManager(i.getInstance(DB.class), i.getInstance(Configuration.class),
						i.getInstance(Engine.class)))).singleton();
		injector.register(SiteMediaManager.class,
				i -> provide(() -> siteMediaManager(i.getInstance(DB.class), i.getInstance(Constants.DiScopes.ASSETS, Path.class),
						i.getInstance(Theme.class), i.getInstance(Configuration.class),
						i.getInstance(EventBus.class)))).singleton();
		injector.register(MediaService.class,
				i -> provide(() -> mediaService(i.getInstance(Constants.DiScopes.ASSETS, Path.class)))).singleton();
		injector.register(RequestContextFactory.class, this::requestContextFactory).singleton();
		injector.register(RenderContentFunction.class,
				i -> renderContentFunction(i.getInstance(ContentResolver.class),
						i.getInstance(RequestContextFactory.class))).singleton();
		injector.register(ContentRenderer.class,
				i -> contentRenderer(i, i.getInstance(FileDB.class), i.getInstance(SiteProperties.class),
						i.getInstance(ModuleManager.class), i.getInstance(ContentRepository.class),
						i.getInstance(CollectionRepository.class))).singleton();
		injector.register(ContentResolver.class,
				i -> contentResolver(i.getInstance(ContentRenderer.class), i.getInstance(ContentRepository.class),
						i.getInstance(VariantSelector.class))).singleton();
		injector.register(CollectionResolver.class,
				i -> collectionResolver(i.getInstance(ContentRenderer.class), i.getInstance(CollectionRepository.class),
						i.getInstance(Configuration.class))).singleton();
		injector.register(VariantSelectorConfigurationRepository.class,
				i -> variantSelectorConfigurationRepository(i.getInstance(ContentRepository.class),
						i.getInstance(MutableContentRepository.class))).singleton();
		injector.register(ConfigurableVariantSelector.class,
				i -> configurableVariantSelector(i.getInstance(VariantSelectorConfigurationRepository.class),
						i.getInstance(ModuleManager.class))).singleton();
		injector.register(VariantSelector.class,
				i -> variantSelector(i.getInstance(ConfigurableVariantSelector.class))).singleton();
		injector.register(ViewResolver.class,
				i -> viewResolver(i.getInstance(ContentRenderer.class), i.getInstance(ContentRepository.class))).singleton();
		injector.register(CronJobContext.class, _ -> cronJobContext()).singleton();
		injector.register(MailService.class, i -> mailServife(i.getInstance(DB.class))).singleton();
		injector.register(VisitorContextService.class, _ -> visitorContextService()).eager();
	}

	public Workflow workflow () {
		WorkflowInstance wf = new WorkflowInstance("release", "Release", new DefaultWFStatusProvider());
		
				wf.addTransition(new WFTransition(
				"publish",
				"Publish",
                "Sets the state of the node to published",
				"published",
				(node) -> node.data().put("status", DefaultWFStatusProvider.STATUS_PUBLISHED),
				(node) -> node.data().getOrDefault("status", DefaultWFStatusProvider.STATUS_DRAFT).equals(DefaultWFStatusProvider.STATUS_DRAFT),
				java.util.Set.of(com.condation.cms.api.auth.Permissions.WORKFLOW_PUBLISH)
		));

		wf.addTransition(new WFTransition(
				"unpublish",
				"Unpublish",
                "Sets the state of the node to draft",
				"draft",
				(node) -> node.data().put("status", DefaultWFStatusProvider.STATUS_DRAFT),
				(node) -> node.data().getOrDefault("status", DefaultWFStatusProvider.STATUS_DRAFT).equals(DefaultWFStatusProvider.STATUS_PUBLISHED)
		));
		
		return wf;
	}
	
	public Messaging messaging () {
		return new DefaultMessaging(this.siteId);
	}
	
	public ContentNodeMapper contentNodeMapper (ContentRepository contentRepository) {
		return new ContentNodeMapper(contentRepository);
	}
	
	public ContentParser contentParser (Configuration configuration, CacheManager cacheManager) {
		boolean IS_DEV = configuration.get(ServerConfiguration.class).serverProperties().dev();
		if (IS_DEV) {
			return new DefaultContentParser();
		} else {
			return new DefaultContentParser(
                    cacheManager.get(
                            Constants.CacheNames.CONTENT, 
                            new CacheManager.CacheConfig(100L, Duration.ofMinutes(1))));
		}
	}
    
	public ShortCodeParser ShortCodeParser (Configuration configuration, MarkdownRenderer markdownRenderer) {
		var engine = new JexlBuilder()
				.strict(true)
				.cache(512);
		
		boolean IS_DEV = configuration.get(ServerConfiguration.class).serverProperties().dev();
		
		if (IS_DEV) {
			engine.silent(false);
		} else {
			engine.silent(true);
		}
		
		return new ShortCodeParser(engine.create(), markdownRenderer);
	}
	
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
	
	public AuthService authService(DB db) {
		return new AuthService(db.getFileSystem().hostBase());
	}

	public MenuService menuService(DB db, CacheManager cacheManager, EventBus eventBus) {
		ICache<String, Menu> menuCache = cacheManager.get(
				Constants.CacheNames.MENU,
				new CacheManager.CacheConfig(100L, Duration.ofMinutes(5)));
		return new CachingMenuService(
				new FileMenuService(db.getFileSystem().hostBase()),
				menuCache,
				eventBus);
	}
	
	public Path assetsPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.ASSETS);
	}

	public Path publicPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.PUBLIC);
	}
    
	public Path templatesPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.TEMPLATES);
	}

	public Path contentPath(DB db) {
		return db.getFileSystem().resolve(Constants.Folders.CONTENT);
	}

	public FileDB fileDb(DB db) throws IOException {
		return (FileDB) db;
	}

	public MessageSource messages(SiteProperties site, DB db, CacheManager cacheManager) throws IOException {
		ICache<String, String> cache = cacheManager.get("messages", new CacheManager.CacheConfig(500l, Duration.ofMinutes(5)));
		var messages = new DefaultMessageSource(site, db.getFileSystem().resolve("messages/"), cache);
		return messages;
	}

	public DB fileDb(DefaultContentParser contentParser, Configuration configuration, EventBus eventBus) throws IOException {
		var db = new FileDB(hostBase, eventBus, (file) -> {
			try {
				ReadOnlyFile cmsFile = new NIOReadOnlyFile(file, hostBase.resolve(Constants.Folders.CONTENT));
				return contentParser.parseMeta(cmsFile);
			} catch (IOException ioe) {
				log.error(null, ioe);
				throw new RuntimeException(ioe);
			}
		}, configuration);
		db.init();
		return db;
	}

	public ContentStore contentStore(DB db) {
		return new FileSystemContentStore(db.getFileSystem());
	}

	public MutableContentRepository mutableContentRepository(
			DB db,
			ContentStore contentStore,
			ContentParser contentParser) {
		return new FileSystemContentRepository(
				db.getContent(),
				db.getFileSystem(),
				contentStore,
				contentParser);
	}

	public ContentRepository contentRepository(MutableContentRepository repository) {
		return repository;
	}

	public MutableCollectionRepository mutableCollectionRepository(DB db) {
		return db.getCollectionRepository();
	}

	public CollectionRepository collectionRepository(MutableCollectionRepository repository) {
		return repository;
	}

	public ExtensionManager extensionManager(DB db, Configuration configuration, Engine engine) throws IOException {
		var extensionManager = new ExtensionManager(
				db, 
				configuration.get(ServerConfiguration.class).serverProperties(), 
				engine
		);

		return extensionManager;
	}

	public SiteMediaManager siteMediaManager(DB db, Path assetBase, Theme theme, Configuration configuration, EventBus eventbus) {
		var mediaManager = new SiteMediaManager(assetBase, db.getFileSystem().resolve("temp"), theme, configuration);
		eventbus.register(ConfigurationReloadEvent.class, mediaManager);
		return mediaManager;
	}

	public MediaService mediaService(Path assetBase) throws IOException {
		return new FileMediaService(assetBase);
	}

	public RequestContextFactory requestContextFactory(Injector injector) {
		return new RequestContextFactory(
				injector
		);
	}
	
	public RenderContentFunction renderContentFunction (ContentResolver contentResolver, RequestContextFactory requestContextFatory) {
		return new DefaultRenderContentFunction(contentResolver, requestContextFatory);
	}

	public ContentRenderer contentRenderer(Injector injector, FileDB db,
			SiteProperties siteProperties, ModuleManager moduleManager,
			ContentRepository contentRepository,
			CollectionRepository collectionRepository) {
		return new DefaultContentRenderer(
				() -> injector.getInstance(TemplateEngine.class),
				db,
				siteProperties,
				moduleManager,
				contentRepository,
				collectionRepository);
	}

	public ContentResolver contentResolver(ContentRenderer contentRenderer,
			ContentRepository contentRepository, VariantSelector variantSelector) {
		return new ContentResolver(contentRenderer, contentRepository, variantSelector);
	}

	public CollectionResolver collectionResolver(
			ContentRenderer contentRenderer,
			CollectionRepository collectionRepository,
			Configuration configuration) {
		return new CollectionResolver(contentRenderer, collectionRepository, configuration);
	}

	public VariantSelectorConfigurationRepository variantSelectorConfigurationRepository(
			ContentRepository contentRepository,
			MutableContentRepository mutableContentRepository
	) {
		return new VariantSelectorConfigurationRepository(contentRepository, mutableContentRepository);
	}

	public ConfigurableVariantSelector configurableVariantSelector(
			VariantSelectorConfigurationRepository configurationRepository,
			ModuleManager moduleManager
	) {
		return new ConfigurableVariantSelector(
				new DefaultVariantSelector(),
				configurationRepository,
				moduleManager
		);
	}

	public VariantSelector variantSelector(ConfigurableVariantSelector selector) {
		return selector;
	}

	public ViewResolver viewResolver(ContentRenderer contentRenderer,
			ContentRepository contentRepository) {
		return new ViewResolver(contentRenderer, contentRepository);
	}
	
	public CronJobContext cronJobContext() {
		final CronJobContext cronJobContext = new CronJobContext();
		
		return cronJobContext;
	}
	
	
	public MailService mailServife (DB db) {
		return new DefaultMailService(db);
	}
    
    public VisitorContextService visitorContextService () {
        var service = new VisitorContextService();
        service.create("curl/8.7.1");
        return service;
    }
}
