package com.condation.cms.templates.module;

/*-
 * #%L
 * cms-templates
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
import com.condation.cms.api.cache.CacheManager;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.extensions.RegisterTemplateComponentExtensionPoint;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.template.TemplateEngine;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.DynamicConfiguration;
import com.condation.cms.templates.TemplateEngineFactory;
import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.components.TemplateComponents;
import com.condation.cms.templates.loaders.CompositeTemplateLoader;
import com.condation.cms.templates.loaders.FileTemplateLoader;
import com.condation.modules.api.ModuleManager;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author t.marx
 */
public class CMSModuleTemplateEngine implements TemplateEngine {

	CMSTemplateEngine templateEngine;

	CacheManager cacheManager;

	DB db;

	boolean devMode = false;

	Theme theme = null;

	private CMSModuleTemplateEngine() {

	}

	public static CMSModuleTemplateEngine create(final DB db, final Theme theme, CacheManager cacheManager, boolean devMode) {

		var engine = new CMSModuleTemplateEngine();

		engine.db = db;
		engine.devMode = devMode;
		engine.cacheManager = cacheManager;
		engine.theme = theme;

		engine.initTemplateEngine();

		return engine;
	}

	private void initTemplateEngine() {

		var loaders = new ArrayList<TemplateLoader>();
		loaders.add(new FileTemplateLoader(db.getFileSystem().resolve("templates/")));

		if (!theme.empty()) {
			var themeLoader = new FileTemplateLoader(theme.templatesPath());
			loaders.add(themeLoader);

			if (theme.getParentTheme() != null) {
				var parentLoader = new FileTemplateLoader(theme.getParentTheme().templatesPath());
				loaders.add(parentLoader);
			}
		}

		CompositeTemplateLoader templateLoader = new CompositeTemplateLoader(loaders);

		templateEngine = TemplateEngineFactory.newInstance(templateLoader)
				.cache(cacheManager.get("templates", new CacheManager.CacheConfig(100l, Duration.ofMinutes(1))))
				.defaultFilters()
				.defaultTags()
				.devMode(devMode)
				.create();
	}

	@Override
	public void invalidateCache() {
		templateEngine.invalidateTemplateCache();
	}

	@Override
	public void updateTheme(Theme theme) {
		initTemplateEngine();
		templateEngine.invalidateTemplateCache();
	}

	@Override
	public String render(String template, Model model) throws IOException {
		var cmsTemplate = templateEngine.getTemplate(template);

		return cmsTemplate.evaluate(model.values, createDynamicConfiguration(model));
	}

	private DynamicConfiguration createDynamicConfiguration(Model model) {
		DynamicConfiguration dynamicConfig = new DynamicConfiguration(
				createTemplateComponents(model.requestContext), model.requestContext);
		return dynamicConfig;
	}
	
	private TemplateComponents createTemplateComponents(RequestContext requestContext) {
		Map<String, Function<Parameter, String>> components = new HashMap<>();

		var injector = requestContext.get(InjectorFeature.class).injector();
		
		injector.getInstance(ModuleManager.class)
				.extensions(RegisterTemplateComponentExtensionPoint.class)
				.forEach(extension -> components.putAll(extension.components()));

		var wrapper = requestContext.get(TemplateHooks.class).getComponents(components);
		return new TemplateComponents(wrapper.getComponents());
	}

	@Override
	public String renderFromString(String templateString, Model model) throws IOException {
		var template = templateEngine.getTemplateFromString(templateString);
		return template.evaluate(model.values, createDynamicConfiguration(model));
	}
	
	

}
