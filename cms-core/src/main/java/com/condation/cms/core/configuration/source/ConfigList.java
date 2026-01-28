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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * ConfigList - Encapsulates a List and resolves environment variables on access.
 * 
 * Features:
 * - Lazy Resolution: Variables are only resolved on access
 * - Recursive Wrapping: Nested Maps are automatically wrapped in ConfigMap
 * - Transparent: Behaves like a normal List
 * - Type Support: Handles Strings, Maps, Lists, and other data types
 * 
 * Example:
 * List<Object> original = List.of("${env:DB_HOST}", Map.of("key", "${env:DB_PORT}"));
 * ConfigList configList = new ConfigList(original);
 * String host = (String) configList.get(0);  // Returns resolved value
 * 
 * @author thorstenmarx
 */
class ConfigList extends ArrayList<Object> {
    
    private final EnvironmentVariables ENV;
    
    /**
     * Creates a ConfigList from an existing collection.
     * 
     * @param original the original collection to wrap
     * @throws NullPointerException if original is null
     */
    ConfigList(Collection<?> original) {
        super(original);
		ENV = new EnvironmentVariables(ServerUtil.getHome());
    }

    @Override
    public Object get(int index) {
        return resolveValue(super.get(index));
    }

    @Override
    public Iterator<Object> iterator() {
        // Return iterator with resolved values
        return new ConfigListIterator(0);
    }

    @Override
    public ListIterator<Object> listIterator() {
        // Return list iterator with resolved values
        return new ConfigListIterator(0);
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        // Return list iterator with resolved values starting at index
        return new ConfigListIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        // Return ConfigList sublist
        return new ConfigList(super.subList(fromIndex, toIndex));
    }

    /**
     * Resolves environment variables and wraps nested structures.
     * 
     * @param value the value to resolve
     * @return resolved value (with env vars replaced and nested structures wrapped)
     */
    private Object resolveValue(Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case String s -> ENV.resolveEnvVars(s);
            case Map<?, ?> m -> new ConfigMap((Map<String, Object>) m);
            case List<?> l -> new ConfigList(l);
            default -> value;
        };
    }

    /**
     * Custom ListIterator that returns resolved values.
     */
    private class ConfigListIterator implements ListIterator<Object> {
        
        private final ListIterator<Object> delegate = ConfigList.super.listIterator();
        
        ConfigListIterator(int index) {
            while (delegate.nextIndex() < index) {
                delegate.next();
            }
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Object next() {
            return resolveValue(delegate.next());
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public Object previous() {
            return resolveValue(delegate.previous());
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            delegate.remove();
        }

        @Override
        public void set(Object e) {
            delegate.set(e);
        }

        @Override
        public void add(Object e) {
            delegate.add(e);
        }
    }
}
