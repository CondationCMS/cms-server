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

import com.condation.cms.api.ThemeProperties;
import com.condation.cms.core.configuration.configs.SimpleConfiguration;
import java.util.List;

/**
 *
 * @author t.marx
 */
public class ExtendedThemeProperties implements ThemeProperties {
	
	private final SimpleConfiguration configuration;
	
	public ExtendedThemeProperties(SimpleConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Double version() {
		return configuration.getDouble("version");
	}

	@Override
	public String parent() {
		return configuration.getString("parent");
	}
	
	@Override
	public String name() {
		return configuration.getString("name");
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
	
	
}
