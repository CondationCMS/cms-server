package com.github.thmarx.cms.api;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class YamlProperties {
	
	protected final Map<String, Object> properties;

	protected YamlProperties (final Map<String, Object> properties) {
		this.properties = properties;
	}
	
	public Object get(final String name) {
		return properties.get(name);
	}

	public <T> T getOrDefault(final String name, final T defaultValue) {
		return (T) properties.getOrDefault(name, defaultValue);
	}

	protected Map<String, Object> getSubMap(final String name) {
		return (Map<String, Object>) properties.getOrDefault(name, Collections.emptyMap());
	}
	
}
