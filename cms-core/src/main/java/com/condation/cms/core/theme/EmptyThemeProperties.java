package com.condation.cms.core.theme;

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

import com.condation.cms.api.ThemeProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class EmptyThemeProperties implements ThemeProperties {

	private final Map<String, Object> properties;
	
	@Override
	public Double version() {
		return (Double) properties.get("versions");
	}
	
	@Override
	public Object get(String field) {
		return properties.get(field);
	}

	@Override
	public String parent() {
		return (String) properties.get("parent");
	}

	@Override
	public String name() {
		return (String) properties.get("name");
	}
	
	@Override
	public String templateEngine() {
		return (String) properties.get("template.engine");
	}

	@Override
	public List<String> activeModules() {
		return (List<String>) properties.getOrDefault("modules.active", List.of());
	}
	
}
