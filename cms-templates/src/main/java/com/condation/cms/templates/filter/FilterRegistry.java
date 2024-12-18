package com.condation.cms.templates.filter;

/*-
 * #%L
 * templates
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

import java.util.HashMap;
import java.util.Map;

public class FilterRegistry {
    private final Map<String, Filter> filters = new HashMap<>();

	public boolean empty () {
		return filters.isEmpty();
	}
	
    public void register(String name, Filter filter) {
        filters.put(name, filter);
    }

    public Filter get(String name) {
        return filters.get(name);
    }

    public boolean exists(String name) {
        return filters.containsKey(name);
    }
}
