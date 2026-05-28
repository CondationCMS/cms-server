package com.condation.cms.content.tags.annotation;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.annotations.Tag;
import com.condation.cms.api.model.Parameter;
import com.google.common.base.Strings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * Scans an object for {@link Tag}-annotated methods and registers them into a
 * tag map.
 * <p>
 * Each method must have the signature {@code String method(Parameter param)}.
 * The registration key is built from the annotation's {@code namespace} and
 * {@code value}: {@code "namespace:tagname"}.
 *
 * @author t.marx
 */
@Slf4j
public class AnnotationTagRegistrar {

    public void register(Object handler, Map<String, Function<Parameter, String>> tagMap) {
        if (handler == null) {
            return;
        }

        for (Method method : handler.getClass().getMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (!method.isAnnotationPresent(Tag.class)) {
                continue;
            }

            Tag annotation = method.getAnnotation(Tag.class);
            String key = buildKey(annotation);
            Function<Parameter, String> fn = buildFunction(handler, method, key);
            if (fn != null) {
                tagMap.put(key, fn);
            }
        }
    }

    private Function<Parameter, String> buildFunction(Object target, Method method, String key) {
        java.lang.reflect.Parameter[] params = method.getParameters();

        // context style: single Parameter argument
        if (params.length == 1 && Parameter.class.isAssignableFrom(params[0].getType())) {
            return param -> invoke(target, method, key, param);
        }

        // named-params style: all parameters carry @Param
        String[] names = extractParamNames(params);
        if (names != null) {
            return param -> {
                Object[] args = resolveArgs(param, params, names);
                return invoke(target, method, key, args);
            };
        }

        log.warn("@Tag method '{}' in '{}' has unsupported signature — skipped",
                method.getName(), target.getClass().getSimpleName());
        return null;
    }

    private String[] extractParamNames(java.lang.reflect.Parameter[] params) {
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

    private Object[] resolveArgs(Parameter param, java.lang.reflect.Parameter[] params, String[] names) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = param.getOrDefault(names[i], null);
        }
        return args;
    }

    private String invoke(Object target, Method method, String key, Object... args) {
        try {
            return (String) method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("error calling tag '{}'", key, e);
            throw new RuntimeException("Error calling tag: " + key, e);
        }
    }

    private String buildKey(Tag annotation) {
        String namespace = annotation.namespace();
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE;
        }
        return "%s:%s".formatted(namespace, annotation.value());
    }
}
