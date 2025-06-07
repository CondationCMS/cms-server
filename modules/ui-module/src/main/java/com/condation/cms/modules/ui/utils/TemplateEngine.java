package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.cache.CacheProvider;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.TemplateEngineFactory;
import com.condation.cms.templates.loaders.ClasspathTemplateLoader;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
public class TemplateEngine {

	CMSTemplateEngine templateEngine;

	public TemplateEngine(CacheManager cacheManager) {

		templateEngine = TemplateEngineFactory
				.newInstance(new ClasspathTemplateLoader("manager"))
				.cache(cacheManager.get("ui/templates", new CacheManager.CacheConfig(100l, Duration.ofSeconds(60))))
				.defaultFilters()
				.defaultTags()
				.create();
	}

	public String render(String templateName, Map<String, Object> model) throws IOException {
		var template = templateEngine.getTemplate(templateName);

		return template.evaluate(model);
	}
}
