package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.cache.ICache;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.condation.cms.templates.error.ErrorHandler;
import com.condation.cms.templates.error.LenientErrorHandler;
import com.condation.cms.templates.error.StrictErrorHandler;
import com.condation.cms.templates.filter.Filter;
import com.condation.cms.templates.filter.FilterRegistry;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the template engine including performance and behavior settings.
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

	/**
	 * Maximum depth for recursive template rendering to prevent stack overflow.
	 * Default: 100
	 */
	@Getter
	@Setter
	private int maxRenderDepth = 100;

	/**
	 * Size of the JEXL expression cache.
	 * Defaults to 512 in dev mode, 1024 in production mode.
	 */
	@Getter
	@Setter
	private int expressionCacheSize;

	/**
	 * JEXL safe mode - when true, prevents access to certain Java features.
	 * Defaults to false in dev mode, true in production mode.
	 */
	@Getter
	@Setter
	private boolean jexlSafeMode;

	/**
	 * JEXL strict mode - when true, throws exceptions on undefined variables.
	 * Default: false
	 */
	@Getter
	@Setter
	private boolean jexlStrict = false;

	/**
	 * JEXL silent mode - when true, suppresses exceptions during expression evaluation.
	 * Defaults to false in dev mode, true in production mode.
	 */
	@Getter
	@Setter
	private boolean jexlSilent;

	/**
	 * Error handler for template errors.
	 * Defaults to StrictErrorHandler in dev mode, LenientErrorHandler in production mode.
	 */
	@Getter
	@Setter
	private ErrorHandler errorHandler;

	public TemplateConfiguration (final boolean devMode) {
		this.devMode = devMode;

		// Set defaults based on dev/prod mode
		if (devMode) {
			this.expressionCacheSize = 512;
			this.jexlSafeMode = false;
			this.jexlSilent = false;
			this.errorHandler = new StrictErrorHandler();
		} else {
			this.expressionCacheSize = 1024;
			this.jexlSafeMode = true;
			this.jexlSilent = true;
			this.errorHandler = new LenientErrorHandler();
		}
	}
	
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
		
        if (tag.alternateTagName().isPresent()) {
            registeredTags.put(tag.alternateTagName().get(), tag);
        }
        
		return this;
	}
	
	public boolean hasTag (String tagName) {
		return registeredTags.containsKey(tagName);
	}
	
	public Optional<Tag> getTag (String tagName) {
		return Optional.ofNullable(registeredTags.get(tagName));
	}
}
