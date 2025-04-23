package com.condation.cms.templates.components;

/*-
 * #%L
 * cms-content
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.api.annotations.TemplateComponent;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import java.lang.reflect.Method;
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

		Class<?> clazz = handler.getClass();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(TemplateComponent.class)) {
				if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == Parameter.class) {
					method.setAccessible(true);
					var annotation = method.getAnnotation(TemplateComponent.class);
					String key = annotation.value();

					componentMap.put(key, param -> {
						try {
							return (String) method.invoke(handler, param);
						} catch (Exception e) {
							throw new RuntimeException("Error calling component: " + key, e);
						}
					});
				} else {
					log.error("ignore methode" + method.getName() + " – wrong signature.");
				}
			}
		}
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
