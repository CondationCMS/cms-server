package com.condation.cms.templates.components;

/*-
 * #%L
 * CMS Templates
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
import com.condation.cms.api.annotations.TemplateFunction;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.utils.ParamAnnotationUtil;
import com.google.common.base.Strings;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class TemplateFunctions {

	@Getter
	private final FunctionMap functionMap;

	public TemplateFunctions() {
		this.functionMap = new FunctionMap();
	}

	public void register(String name, Function<Parameter, ?> templateFunction) {
        register(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, name, templateFunction);
    }

    public void register(String namespace, String name, Function<Parameter, ?> templateFunction) {
		functionMap.put(namespace, name, templateFunction);
	}
    
    public void register(Map<String, Function<Parameter, ?>> templateFunctions) {
        register(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE, templateFunctions);
    }
    public void register(String namespace, Map<String, Function<Parameter, ?>> templateFunctions) {
        this.functionMap.putAll(namespace, templateFunctions);
    }

	public void register(List<Object> handlers) {
		if (handlers == null || handlers.isEmpty()) {
			return;
		}
		handlers.forEach(this::register);
	}

	public void register(Object handler) {
		if (handler == null) {
			return;
		}
		for (Method method : handler.getClass().getMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!method.isAnnotationPresent(TemplateFunction.class)) {
				continue;
			}
			TemplateFunction annotation = method.getAnnotation(TemplateFunction.class);
			String namespace = Strings.isNullOrEmpty(annotation.namespace())
					? Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE
					: annotation.namespace();
			String name = annotation.value();
			Function<Parameter, ?> fn = buildFunction(handler, method, name);
			if (fn != null) {
				functionMap.put(namespace, name, fn);
			}
		}
	}

	private Function<Parameter, ?> buildFunction(Object target, Method method, String name) {
		java.lang.reflect.Parameter[] params = method.getParameters();

		// no-arg style
		if (params.length == 0) {
			return param -> ParamAnnotationUtil.invokeOrThrow(target, method, name);
		}

		// context style: single Parameter argument
		if (ParamAnnotationUtil.isContextStyle(params, Parameter.class)) {
			return param -> ParamAnnotationUtil.invokeOrThrow(target, method, name, param);
		}

		// named-params style: all parameters carry @Param
		String[] names = ParamAnnotationUtil.extractParamNames(params);
		if (names != null) {
			return param -> ParamAnnotationUtil.invokeOrThrow(target, method, name,
					ParamAnnotationUtil.resolveArgs(param, names));
		}

		log.warn("@TemplateFunction method '{}' in '{}' has unsupported signature — skipped",
				method.getName(), target.getClass().getSimpleName());
		return null;
	}

	public Set<FunctionMap.ExtFunction> getFunctions() {
		return functionMap.functions();
	}

    /*	public Object execute(String name, Map<String, Object> parameters, RequestContext requestContext) {
    if (!functionMap.has(name)) {
    return "";
    }
    try {
    Parameter params;
    if (parameters != null) {
    params = new Parameter(parameters);
    } else {
    params = new Parameter();
    }
    return functionMap.get(name).apply(params);
    } catch (Exception e) {
    log.error("", e);
    }
    return "";
    }*/
}
