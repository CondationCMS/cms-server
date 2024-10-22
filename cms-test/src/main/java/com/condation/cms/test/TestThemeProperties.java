package com.condation.cms.test;

import com.condation.cms.api.Constants;
import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.ThemeProperties;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class TestThemeProperties implements ThemeProperties {

	private final Map<String, Object> values;

	public TestThemeProperties(Map<String, Object> values) {
		this.values = values;
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
