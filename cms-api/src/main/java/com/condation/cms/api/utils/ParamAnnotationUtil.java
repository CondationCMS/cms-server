package com.condation.cms.api.utils;

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

import com.condation.cms.api.annotations.Param;
import com.google.common.base.Strings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shared reflection helpers for annotation-driven method registration
 * ({@code @Param}, context-style, no-arg).
 *
 * @author t.marx
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParamAnnotationUtil {

    /**
     * Extracts {@link Param} names from every parameter in {@code params}.
     * <ul>
     *   <li>Returns an empty array when {@code params} is empty (no-arg style).</li>
     *   <li>Returns {@code null} when any parameter lacks {@code @Param} (unsupported signature).</li>
     * </ul>
     */
    public static String[] extractParamNames(Parameter[] params) {
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

    /**
     * Resolves positional method arguments from {@code source} using {@code names} as keys.
     * Missing keys resolve to {@code null}.
     */
    public static Object[] resolveArgs(Map<String, Object> source, String[] names) {
        Object[] args = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            args[i] = source.get(names[i]);
        }
        return args;
    }

    /**
     * Returns {@code true} when {@code params} contains exactly one entry whose type
     * is assignable from {@code contextType}.
     */
    public static boolean isContextStyle(Parameter[] params, Class<?> contextType) {
        return params.length == 1 && contextType.isAssignableFrom(params[0].getType());
    }

    /**
     * Builds a {@code "namespace:name"} registration key, substituting
     * {@code defaultNamespace} when the namespace is blank.
     */
    public static String buildNamespaceKey(String namespace, String name, String defaultNamespace) {
        String ns = Strings.isNullOrEmpty(namespace) ? defaultNamespace : namespace;
        return "%s:%s".formatted(ns, name);
    }

    /**
     * Invokes {@code method} on {@code target} with {@code args}.
     * Logs the error and returns {@code null} on failure.
     */
    public static Object invokeOrNull(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("error invoking '{}' on '{}'", method.getName(), target.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * Invokes {@code method} on {@code target} with {@code args}.
     * Logs the error and rethrows as {@link RuntimeException} on failure.
     */
    public static Object invokeOrThrow(Object target, Method method, String label, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("error invoking '{}'", label, e);
            throw new RuntimeException("Error invoking: " + label, e);
        }
    }
}
