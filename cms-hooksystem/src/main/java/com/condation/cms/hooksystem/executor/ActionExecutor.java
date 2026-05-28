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

import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.hooksystem.registry.ActionRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes all action hooks registered under a given name.
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class ActionExecutor {

    private final ActionRegistry registry;

    public <T> List<T> execute(String name, Map<String, Object> arguments) {
        var context = new ActionContext<T>(new HashMap<>(arguments), new ArrayList<>());

        registry.get(name).forEach(hook -> {
            try {
				T result = (T)hook.function().apply(context);
                if (result != null) {
                    context.results().add(result);
                }
            } catch (Exception e) {
                log.error("error executing action hook '{}'", name, e);
            }
        });

        return context.results();
    }
}
