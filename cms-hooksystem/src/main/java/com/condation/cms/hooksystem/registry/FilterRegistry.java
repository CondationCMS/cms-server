package com.condation.cms.hooksystem.registry;

/*-
 * #%L
 * CMS Api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.hooks.FilterFunction;
import com.condation.cms.hooksystem.FilterHook;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.List;

/**
 * Stores and provides sorted access to registered filter hooks.
 *
 * @author t.marx
 */
public class FilterRegistry {

    private final Multimap<String, FilterHook> hooks = ArrayListMultimap.create();

    public <T> void register(String name, FilterFunction<T> function, int priority) {
        hooks.put(name, new FilterHook<>(name, priority, function));
    }

    public List<FilterHook> get(String name) {
        return hooks.get(name).stream()
                .sorted(Comparator.comparingInt(FilterHook::priority))
                .toList();
    }

    public void putAll(FilterRegistry source) {
        hooks.putAll(source.hooks);
    }
}
