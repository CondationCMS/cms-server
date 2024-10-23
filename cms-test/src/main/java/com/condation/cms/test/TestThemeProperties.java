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

import com.condation.cms.api.ThemeProperties;
import java.util.List;
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
	public Object get(String field) {
		return values.get(field);
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
