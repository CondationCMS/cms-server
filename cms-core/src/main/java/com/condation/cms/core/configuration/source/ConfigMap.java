package com.condation.cms.core.configuration.source;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.core.configuration.EnvironmentVariables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thorstenmarx
 */
class ConfigMap extends HashMap<String, Object> {

	private final EnvironmentVariables ENV;

	ConfigMap(Map<String, Object> original) {
		putAll(original);
		ENV = new EnvironmentVariables(ServerUtil.getHome());
	}
	
	@Override
	public Object get(Object key) {
		if (!containsKey(key)) {
			return null;
		}
		var value = super.get(key);

		return resolveValue(value);
	}

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		if (!super.containsKey(key)) {
			return defaultValue;
		}
		var value = super.getOrDefault(key, defaultValue);

		return resolveValue(value);
	}

	// Extrahiere in private Hilfsmethode
	private Object resolveValue(Object value) {
		if (value == null) {
			return null;
		}
		return switch (value) {
			case String s ->
				ENV.resolveEnvVars(s);
			case Map<?, ?> m ->
				new ConfigMap((Map<String, Object>) m);
			case List<?> l -> new ConfigList(l);
			default ->
				value;
		};
	}

}
