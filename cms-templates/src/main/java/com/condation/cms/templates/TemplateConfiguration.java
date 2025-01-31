package com.condation.cms.templates;

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

import com.condation.cms.api.cache.ICache;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.condation.cms.templates.filter.Filter;
import com.condation.cms.templates.filter.FilterRegistry;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author t.marx
 */
public class TemplateConfiguration {

	private final Map<String, Tag> registeredTags = new HashMap<>();
	@Getter
	private final FilterRegistry filterRegistry = new FilterRegistry();
	
	@Getter
	@Setter
	private TemplateLoader templateLoader;
	
	@Getter
	private TemplateCache templateCache = null;
	
	@Getter
	private boolean devMode = false;
	
	public boolean hasTags () {
		return !registeredTags.isEmpty();
	}
	
	public boolean hasFilters () {
		return !filterRegistry.empty();
	}
	
	public TemplateConfiguration setCache (ICache<String, Template> cache) {
		if (templateCache == null) {
			templateCache = new TemplateCache(cache);
		}
		return this;
	}
	
	public TemplateConfiguration setDevMode (boolean devMode) {
		this.devMode = devMode;
		return this;
	}
	
	public TemplateConfiguration registerFilter (String name, Filter filter) {
		filterRegistry.register(name, filter);
		return this;
	}

	public TemplateConfiguration registerTag (Tag tag) {
		registeredTags.put(tag.getTagName(), tag);
		
		return this;
	}
	
	public boolean hasTag (String tagName) {
		return registeredTags.containsKey(tagName);
	}
	
	public Optional<Tag> getTag (String tagName) {
		return Optional.ofNullable(registeredTags.get(tagName));
	}
}
