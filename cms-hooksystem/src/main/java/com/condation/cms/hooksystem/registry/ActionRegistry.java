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

import com.condation.cms.api.hooks.ActionFunction;
import com.condation.cms.hooksystem.ActionHook;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Stores and provides sorted access to registered action hooks.
 *
 * @author t.marx
 */
public class ActionRegistry {

    private final Multimap<String, ActionHook> hooks = ArrayListMultimap.create();

    public <T> void register(String name, ActionFunction<T> function, int priority) {
        hooks.put(name, new ActionHook<>(name, priority, function));
    }

    public List<ActionHook> get(String name) {
        return hooks.get(name).stream()
                .sorted(Comparator.comparingInt(ActionHook::priority))
                .toList();
    }

    public void putAll(ActionRegistry source) {
        hooks.putAll(source.hooks);
    }
}
