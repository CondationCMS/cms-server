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
import com.condation.cms.api.annotations.TemplateComponent;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.ParamAnnotationUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class TemplateComponents {

	private final ComponentMap componentMap;

	public TemplateComponents() {
		this.componentMap = new ComponentMap();
	}

	public void register(String name, Function<Parameter, String> templateComponentFN) {
		componentMap.put(name, templateComponentFN);
	}

	public void register(Map<String, Function<Parameter, String>> components) {
		this.componentMap.putAll(components);
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
			if (!method.isAnnotationPresent(TemplateComponent.class)) {
				continue;
			}
			TemplateComponent annotation = method.getAnnotation(TemplateComponent.class);
			String key = buildKey(annotation);
			Function<Parameter, String> fn = buildFunction(handler, method, key);
			if (fn != null) {
				componentMap.put(key, fn);
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

		log.warn("@TemplateComponent method '{}' in '{}' has unsupported signature — skipped",
				method.getName(), target.getClass().getSimpleName());
		return null;
	}

	private String buildKey(TemplateComponent annotation) {
		return ParamAnnotationUtil.buildNamespaceKey(
				annotation.namespace(), annotation.value(),
				Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE);
	}

	public Set<String> getComponentNames() {
		return componentMap.names();
	}

	public String execute(String name, Map<String, Object> parameters, RequestContext requestContext) {
		if (!componentMap.has(name)) {
			return "";
		}
		try {
			Parameter params;
			if (parameters != null) {
				params = new Parameter(parameters);
			} else {
				params = new Parameter();
			}
			return componentMap.get(name).apply(params);
		} catch (Exception e) {
			log.error("", e);
		}
		return "";
	}
}
