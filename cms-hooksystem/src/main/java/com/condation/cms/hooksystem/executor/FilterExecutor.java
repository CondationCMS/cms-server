package com.condation.cms.hooksystem.executor;

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

import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.hooksystem.registry.FilterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes all filter hooks registered under a given name, passing the value
 * through each filter in priority order.
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class FilterExecutor {

    private final FilterRegistry registry;

    @SuppressWarnings("unchecked")
    public <T> FilterContext<T> execute(String name, T value) {
        final FilterContext<T> result = new FilterContext<>(value);

        registry.get(name).forEach(hook -> {
            try {
                var context = new FilterContext<>(result.value());
                T filtered = (T) hook.function().apply(context);
                result.value(filtered);
            } catch (Exception e) {
                log.error("error executing filter hook '{}'", name, e);
            }
        });

        return result;
    }
}
