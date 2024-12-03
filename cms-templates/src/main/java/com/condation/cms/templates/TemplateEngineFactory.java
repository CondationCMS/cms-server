package com.condation.cms.templates;

import com.condation.cms.api.cache.ICache;
import com.condation.cms.templates.filter.impl.RawFilter;
import com.condation.cms.templates.filter.impl.UpperFilter;

/*-
 * #%L
 * templates
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
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndForTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.macro.EndMacroTag;
import com.condation.cms.templates.tags.ForTag;
import com.condation.cms.templates.tags.IfTag;
import com.condation.cms.templates.tags.macro.ImportTag;
import com.condation.cms.templates.tags.layout.IncludeTag;
import com.condation.cms.templates.tags.macro.MacroTag;
import com.condation.cms.templates.tags.AssignTag;
import com.condation.cms.templates.tags.layout.BlockTag;
import com.condation.cms.templates.tags.layout.EndBlockTag;
import com.condation.cms.templates.tags.layout.ExtendsTag;

/**
 *
 * @author t.marx
 */
public class TemplateEngineFactory {

	private TemplateConfiguration configuration;

	private TemplateEngineFactory() {
		configuration = new TemplateConfiguration();
	}

	public static TemplateEngineFactory newInstance(TemplateLoader templateLoader) {
		var factory = new TemplateEngineFactory();

		factory.configuration.setTemplateLoader(templateLoader);

		return factory;
	}

	public CMSTemplateEngine create() {

		if (!configuration.hasFilters()) {
			defaultFilters();
		}
		if (!configuration.hasTags()) {
			defaultTags();
		}

		return new CMSTemplateEngine(configuration);
	}

	public TemplateEngineFactory defaultTags() {
		configuration.registerTag(new IfTag())
				.registerTag(new ElseIfTag())
				.registerTag(new ElseTag())
				.registerTag(new EndIfTag())
				.registerTag(new ForTag())
				.registerTag(new EndForTag())
				.registerTag(new AssignTag())
				.registerTag(new MacroTag())
				.registerTag(new EndMacroTag())
				.registerTag(new IncludeTag())
				.registerTag(new ImportTag())
				.registerTag(new ExtendsTag())
				.registerTag(new BlockTag())
				.registerTag(new EndBlockTag());
		return this;
	}

	public TemplateEngineFactory defaultFilters() {
		configuration
				.registerFilter(UpperFilter.NAME, new UpperFilter())
				.registerFilter(RawFilter.NAME, new RawFilter());
		return this;
	}

	public TemplateEngineFactory devMode(boolean devMode) {
		configuration.setDevMode(devMode);
		return this;
	}

	public TemplateEngineFactory cache(ICache<String, Template> cache) {
		configuration.setCache(cache);
		return this;
	}
}
