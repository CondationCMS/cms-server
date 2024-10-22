package com.condation.cms.test;

import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
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
	public String name() {
		return (String)values.get("name");
	}

	@Override
	public Double version() {
		return (Double)values.get("version");
	}

	@Override
	public String parent() {
		return (String)values.get("parent");
	}

	@Override
	public String templateEngine() {
		return (String)values.get("template.engine");
	}

	@Override
	public List<String> activeModules() {
		return (List<String>)values.getOrDefault("active.modules", List.of());
	}

}
