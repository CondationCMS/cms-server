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
import com.condation.cms.api.annotations.TemplateFunction;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.AnnotationsUtil;
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
		functionMap.put(name, templateFunction);
	}

	public void register(Map<String, Function<Parameter, ?>> tempaleFunctions) {
		this.functionMap.putAll(tempaleFunctions);
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

		var annotations = AnnotationsUtil.process(handler, TemplateFunction.class, List.of(Parameter.class), Object.class);

		for (var entry : annotations) {
			String key = entry.annotation().value();

			functionMap.put(key, param -> {
				try {
					return entry.invoke(param);
				} catch (Exception e) {
					throw new RuntimeException("Error calling component: " + key, e);
				}
			});
		}
		
		annotations = AnnotationsUtil.process(handler, TemplateFunction.class, List.of(), Object.class);

		for (var entry : annotations) {
			String key = entry.annotation().value();

			functionMap.put(key, param -> {
				try {
					return entry.invoke(null);
				} catch (Exception e) {
					throw new RuntimeException("Error calling component: " + key, e);
				}
			});
		}
	}

	public Set<String> getFunctionNames() {
		return functionMap.names();
	}

	public Object execute(String name, Map<String, Object> parameters, RequestContext requestContext) {
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
	}
}
