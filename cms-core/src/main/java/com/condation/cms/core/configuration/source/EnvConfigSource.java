package com.condation.cms.core.configuration.source;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.utils.EnvUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnvConfigSource implements ConfigSource {

    private final Map<String, Object> envVariables;

	public EnvConfigSource () {
		this(ServerUtil.getHome().toAbsolutePath().toString());
	}
	
    public EnvConfigSource(String path) {
        this.envVariables = EnvUtil.load(path);
    }

    @Override
    public boolean reload() {
        // Environment variables are not expected to change during runtime
        return false;
    }

    @Override
    public boolean exists() {
        // Environment variables are always considered to exist
        return true;
    }

    @Override
    public String getString(String field) {
        return (String) MapUtil.getValue(envVariables, field);
    }

    @Override
    public Object get(String field) {
        return MapUtil.getValue(envVariables, field);
    }

    @Override
    public Map<String, Object> getMap(String field) {
        return MapUtil.getValue(envVariables, field, Collections.emptyMap());
    }

    @Override
    public List<Object> getList(String field) {
        return MapUtil.getValue(envVariables, field, Collections.emptyList());
    }
}
