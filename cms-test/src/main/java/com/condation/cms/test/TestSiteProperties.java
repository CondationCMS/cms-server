package com.condation.cms.test;

/*-
 * #%L
 * cms-test
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
import com.condation.cms.api.TranslationProperties;
import com.condation.cms.api.UIProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class TestSiteProperties implements SiteProperties {

	private final Map<String, Object> values;

	public TestSiteProperties(Map<String, Object> values) {
		this.values = values;
	}

	@Override
	public List<String> hostnames() {
		return List.of((String) values.getOrDefault("hostname", "localhost"));
	}

	@Override
	public String markdownEngine() {
		return (String) values.get("markdown.engine");
	}

	@Override
	public String contextPath() {
		return (String) values.getOrDefault("context_path", "/");
	}
	
	@Override
	public String baseUrl() {
		return (String) values.getOrDefault("baseurl", "");
	}

	@Override
	public String id() {
		return (String) values.getOrDefault("id", "default-site");
	}

	@Override
	public String theme() {
		return (String) values.get("theme");
	}

	@Override
	public String queryIndexMode() {
		return (String) values.getOrDefault("query.index.mode", "MEMORY");
	}

	@Override
	public Locale locale() {
		return Locale.getDefault();
	}

	@Override
	public String language() {
		return (String) values.get("language");
	}

	@Override
	public Object get(String field) {
		return values.get(field);
	}

	@Override
	public <T> T getOrDefault(String field, T defaultValue) {
		return (T) values.getOrDefault(field, defaultValue);
	}
	
	@Override
	public String defaultContentType() {
		return (String) values.getOrDefault("content.type", Constants.DEFAULT_CONTENT_TYPE);
	}

	@Override
	public List<String> contentPipeline() {
		return (List<String>) values.getOrDefault("content.pipeline", Constants.DEFAULT_CONTENT_PIPELINE);
	}

	@Override
	public String cacheEngine() {
		return (String) values.getOrDefault("cache.engine", Constants.DEFAULT_CACHE_ENGINE);
	}

	@Override
	public boolean cacheContent() {
		return (boolean) values.getOrDefault("content.cache", false);
	}

	@Override
	public String templateEngine() {
		return (String)values.get("template.engine");
	}

	@Override
	public List<String> activeModules() {
		return (List<String>)values.getOrDefault("active.modules", List.of());
	}

	@Override
	public UIProperties ui() {
		return new TestUiProperties();
	}

	@Override
	public TranslationProperties translation() {
		return new TestTranslationProperties(true, List.of(), List.of());
	}

	

}
