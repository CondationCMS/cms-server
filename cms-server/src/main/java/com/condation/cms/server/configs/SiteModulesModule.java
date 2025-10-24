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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.cms.api.extensions.MarkdownRendererProviderExtensionPoint;
import com.condation.cms.api.extensions.TemplateEngineProviderExtensionPoint;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.filesystem.FileDB;
import com.condation.modules.api.ModuleManager;
import com.condation.modules.api.ModuleRequestContextFactory;
import com.condation.modules.manager.ModuleAPIClassLoader;
import com.condation.modules.manager.ModuleManagerImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class SiteModulesModule extends AbstractModule {

	private final Path modulesPath;

	@Override
	protected void configure() {
	}

	@Provides
	@Singleton
	public ModuleManager moduleManager(Injector injector, SiteModuleContext context, ModuleRequestContextFactory requestContextFactory) {
		var classLoader = new ModuleAPIClassLoader(ClassLoader.getSystemClassLoader(),
				List.of(
						"org.slf4j",
						"com.condation.cms",
						"com.condation.modules",
						"org.apache.logging",
						"org.graalvm.polyglot",
						"org.graalvm.js",
						"org.eclipse.jetty",
						"jakarta.servlet",
						"com.google",
						"org.w3c"
				));
		var moduleManager = ModuleManagerImpl.builder()
				.setClassLoader(classLoader)
				.setInjector((instance) -> injector.injectMembers(instance))
				.setModulesDataPath(injector.getInstance(FileDB.class).getFileSystem().resolve("modules_data").toFile())
				.setModulesPath(modulesPath.toFile())
				.setContext(context)
				.requestContextFactory(requestContextFactory)
				.build();

		context.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManager));

		return moduleManager;
	}

	@Provides
	@Singleton
	public ModuleRequestContextFactory requestContextFactory() {
		return () -> {
			if (RequestContextScope.REQUEST_CONTEXT.isBound()) {
				return new SiteRequestContext(RequestContextScope.REQUEST_CONTEXT.get());
			} else {
				return new SiteRequestContext(null);
			}
			
		};
	}

	@Provides
	@Singleton
	public SiteModuleContext moduleContext() {
		final SiteModuleContext cmsModuleContext = new SiteModuleContext();

		return cmsModuleContext;
	}

	/**
	 * 
	 *
	 * @param siteProperties
	 * @param moduleManager
	 * @return
	 */
	@Provides
	@Singleton
	public MarkdownRenderer markdownRenderer(SiteProperties siteProperties, ModuleManager moduleManager) {
		var engine = siteProperties.markdownEngine();

		List<MarkdownRendererProviderExtensionPoint> extensions = moduleManager.extensions(MarkdownRendererProviderExtensionPoint.class);
		Optional<MarkdownRendererProviderExtensionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(engine)).findFirst();

		if (extOpt.isPresent()) {
			return extOpt.get().getRenderer();
		}

		throw new RuntimeException("no markdown renderer found");
	}

	private String getTemplateEngine(SiteProperties siteProperties, Theme theme) {
		var site_engine = siteProperties.templateEngine();

		var theme_engine = theme.properties().templateEngine();
		var parent_engine = theme.getParentTheme() != null ? theme.getParentTheme().properties().templateEngine() : null;

		Optional<String> used_engine = Stream.of(site_engine, theme_engine, parent_engine)
				.filter(engine -> engine != null)
				.distinct()
				.reduce((e1, e2) -> {
					throw new RuntimeException("Detected usage of different template engines in site and themes.");
				});
		
		return used_engine.orElse("system");
	}

	@Provides
	@Singleton
	public TemplateEngine resolveTemplateEngine(SiteProperties siteProperties, Theme theme, ModuleManager moduleManager) {
		var engine = getTemplateEngine(siteProperties, theme);

		List<TemplateEngineProviderExtensionPoint> extensions = moduleManager.extensions(TemplateEngineProviderExtensionPoint.class);
		Optional<TemplateEngineProviderExtensionPoint> extOpt = extensions.stream().filter((ext) -> ext.getName().equals(engine)).findFirst();

		if (extOpt.isPresent()) {
			return extOpt.get().getTemplateEngine();
		}
		
		throw new RuntimeException("no template engine found");
	}

	/**
	 * new HookSystem for each request
	 *
	 * @param moduleManager
	 * @return
	 */
	@Provides
	public HookSystem hookSystem(final ModuleManager moduleManager) {
		var hookSystem = new HookSystem();
		
		/*
			moduleManager.extensions(HookSystemRegisterExtensionPoint.class).forEach(extensionPoint -> {
			extensionPoint.register(hookSystem);
			hookSystem.register(extensionPoint);
			});
		*/
		
		return hookSystem;
	}
}
