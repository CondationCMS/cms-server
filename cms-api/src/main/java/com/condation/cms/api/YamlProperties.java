package com.condation.cms.api;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.utils.MapUtil;
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
	
	public void merge (final Map<String, Object> updatedProperties) {
		MapUtil.deepMerge(properties, updatedProperties);
	}
	
	public void update (final Map<String, Object> updatedProperties) {
		this.properties.clear();
		this.properties.putAll(updatedProperties);
	}
	
	public Object get(final String name) {
		return MapUtil.getValue(properties, name);
	}

	public <T> T getOrDefault(final String name, final T defaultValue) {
		return MapUtil.getValue(properties, name, defaultValue);
	}

	protected Map<String, Object> getSubMap(final String name) {
		return getSubMap(name, properties);
	}
	protected Map<String, Object> getSubMap(final String name, Map<String, Object> source) {
		return (Map<String, Object>) source.getOrDefault(name, Collections.emptyMap());
	}
}
