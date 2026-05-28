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
import com.condation.cms.api.annotations.Tag;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.utils.ParamAnnotationUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

        if (ParamAnnotationUtil.isContextStyle(params, Parameter.class)) {
            return param -> (String) ParamAnnotationUtil.invokeOrThrow(target, method, key, param);
        }

        String[] names = ParamAnnotationUtil.extractParamNames(params);
        if (names != null) {
            return param -> (String) ParamAnnotationUtil.invokeOrThrow(target, method, key,
                    ParamAnnotationUtil.resolveArgs(param, names));
        }

        log.warn("@Tag method '{}' in '{}' has unsupported signature — skipped",
                method.getName(), target.getClass().getSimpleName());
        return null;
    }

    private String buildKey(Tag annotation) {
        return ParamAnnotationUtil.buildNamespaceKey(
                annotation.namespace(), annotation.value(),
                Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE);
    }
}
