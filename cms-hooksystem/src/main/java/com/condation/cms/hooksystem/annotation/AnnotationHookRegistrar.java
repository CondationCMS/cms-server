package com.condation.cms.hooksystem.annotation;

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

import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.annotations.Filter;
import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.hooks.ActionFunction;
import com.condation.cms.api.hooks.ActionContext;
import com.condation.cms.api.hooks.FilterContext;
import com.condation.cms.api.hooks.FilterFunction;
import com.condation.cms.hooksystem.registry.ActionRegistry;
import com.condation.cms.hooksystem.registry.FilterRegistry;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scans an object for {@link Action} and {@link Filter} annotated methods and
 * registers them in the corresponding registries.
 * <p>
 * Two method styles are supported for each hook type:
 * <ul>
 *   <li><b>Context style</b> – single {@code ActionContext} / {@code FilterContext} parameter (original API)</li>
 *   <li><b>Named-params style</b> (actions) – every parameter carries {@link Param}; values are resolved
 *       by name from the hook's argument map at invocation time</li>
 *   <li><b>Direct-value style</b> (filters) – single non-{@code FilterContext} parameter; receives the
 *       current filter value directly</li>
 * </ul>
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class AnnotationHookRegistrar {

    private final ActionRegistry actionRegistry;
    private final FilterRegistry filterRegistry;

    public void register(Object source) {
        for (Method method : source.getClass().getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.isAnnotationPresent(Action.class)) {
                registerAction(source, method);
            }
            if (method.isAnnotationPresent(Filter.class)) {
                registerFilter(source, method);
            }
        }
    }

    private void registerAction(Object target, Method method) {
        Action annotation = method.getAnnotation(Action.class);
        ActionFunction<?> fn = buildActionFunction(target, method);
        if (fn != null) {
            actionRegistry.register(annotation.value(), fn, annotation.priority());
        }
    }

    private void registerFilter(Object target, Method method) {
        Filter annotation = method.getAnnotation(Filter.class);
        FilterFunction<?> fn = buildFilterFunction(target, method);
        if (fn != null) {
            filterRegistry.register(annotation.value(), fn, annotation.priority());
        }
    }

    private ActionFunction<?> buildActionFunction(Object target, Method method) {
        Parameter[] params = method.getParameters();

        if (isContextStyle(params, ActionContext.class)) {
            return context -> invoke(target, method, context);
        }

        String[] names = extractParamNames(params);
        if (names != null) {
            return context -> invoke(target, method, resolveArgs(context.arguments(), params, names));
        }

        log.warn("@Action method '{}' in '{}' has unsupported signature — skipped",
                method.getName(), target.getClass().getSimpleName());
        return null;
    }

    private FilterFunction<?> buildFilterFunction(Object target, Method method) {
        Parameter[] params = method.getParameters();

        if (isContextStyle(params, FilterContext.class)) {
            return context -> invoke(target, method, context);
        }

        if (params.length == 1) {
            return context -> invoke(target, method, context.value());
        }

        log.warn("@Filter method '{}' in '{}' has unsupported signature — skipped",
                method.getName(), target.getClass().getSimpleName());
        return null;
    }

    private boolean isContextStyle(Parameter[] params, Class<?> contextType) {
        return params.length == 1 && contextType.isAssignableFrom(params[0].getType());
    }

    private String[] extractParamNames(Parameter[] params) {
        if (params.length == 0) {
            return new String[0];
        }
        String[] names = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            Param p = params[i].getAnnotation(Param.class);
            if (p == null) {
                return null;
            }
            names[i] = p.value();
        }
        return names;
    }

    private Object[] resolveArgs(Map<String, Object> arguments, Parameter[] params, String[] names) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = arguments.get(names[i]);
        }
        return args;
    }

    private Object invoke(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("error invoking hook method '{}' on '{}'",
                    method.getName(), target.getClass().getSimpleName(), e);
            return null;
        }
    }
}
