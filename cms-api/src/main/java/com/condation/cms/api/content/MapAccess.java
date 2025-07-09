package com.condation.cms.api.content;

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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.condation.cms.api.utils.MapUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapAccess implements Map<String, Object> {

    private final Map<String, Object> wrapped;

	public static MapAccess of (Map<String, Object> map) {
		return new MapAccess(map);
	}
	
    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return MapUtil.getValue(wrapped, (String)key) != null;    
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Unimplemented method 'containsValue'");
    }

    @Override
    public Object get(Object key) {
        return MapUtil.getValue(wrapped, (String)key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Unimplemented method 'put'");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("Unimplemented method 'putAll'");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

    @Override
    public Set<String> keySet() {
        return wrapped.keySet();
    }

    @Override
    public Collection<Object> values() {
        return wrapped.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return wrapped.entrySet();
    }

}
