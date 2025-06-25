package com.condation.cms.core.configuration.properties;

/*-
 * #%L
 * cms-core
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
import com.condation.cms.api.SiteProperties;
import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author t.marx
 */
public class ExtendedSiteProperties implements SiteProperties {
	
	private final SimpleConfiguration configuration;
	
	public ExtendedSiteProperties(SimpleConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public List<String> hostnames() {
		// "localhost"
		var hostname = configuration.getValue("hostname", String.class);
		
		if (hostname !=null ) {
			return List.of(hostname);
		}
		
		var hostnames = configuration.getList("hostname", String.class);
		if (hostnames != null && !hostnames.isEmpty()) {
			return hostnames;
		}
		
		return List.of("localhost");
	}

	@Override
	public String markdownEngine() {
		return configuration.getString("markdown.engine", "system");
	}

	@Override
	public String contextPath() {
		return configuration.getString("context_path", "/");
	}

	@Override
	public String id() {
		return configuration.getString("id", "default-site");
	}

	@Override
	public String theme() {
		return configuration.getString("theme");
	}

	@Override
	public String queryIndexMode() {
		return configuration.getString("index.query.mode", "MEMORY");
	}

	@Override
	public Locale locale() {
		if (configuration.get("language") != null) {
			Locale.forLanguageTag((String)configuration.getString("language"));
		}
		return Locale.getDefault();
	}
	
	@Override
	public String language() {
		return configuration.getString("language");
	}

	@Override
	public String defaultContentType() {
		return configuration.getString("content.type", Constants.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public List<String> contentPipeline() {
		var pipeline = configuration.getList("content.pipeline", String.class);
		if (!pipeline.isEmpty()) {
			return pipeline;
		}
		return Constants.DEFAULT_CONTENT_PIPELINE;
	}

	@Override
	public String cacheEngine() {
		return configuration.getString("cache.engine", Constants.DEFAULT_CACHE_ENGINE);
	}

	@Override
	public boolean cacheContent() {
		return configuration.getBoolean("cache.content", Constants.DEFAULT_CONTENT_CACHE_ENABLED);
	}
	
	@Override
	public boolean spaEnabled() {
		return configuration.getBoolean("spa.enabled", false);
	}
	
	@Override
	public String templateEngine() {
		return configuration.getString("template.engine");
	}

	@Override
	public List<String> activeModules() {
		return configuration.getList("modules.active", String.class);
	}
	
	@Override
	public Object get (String field) {
		return configuration.get(field);
	}

	@Override
	public <T> T getOrDefault(String field, T defaultValue) {
		return (T) configuration.getOrDefault(field, defaultValue);
	}
}
