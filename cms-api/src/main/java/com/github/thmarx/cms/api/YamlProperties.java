package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 Marx-Software
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
