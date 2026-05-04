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
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.utils.AnnotationsUtil;
import com.google.common.base.Strings;
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

		var annotations = AnnotationsUtil.process(handler, TemplateFunction.class, List.of(Parameter.class), Object.class);

		for (var entry : annotations) {
			String name = entry.annotation().value();
            String namespace = entry.annotation().namespace();
            if (Strings.isNullOrEmpty(namespace)) {
                namespace = Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE;
            }
			functionMap.put(namespace, name, param -> {
				try {
					return entry.invoke(param);
				} catch (Exception e) {
					throw new RuntimeException("Error calling component: " + name, e);
				}
			});
		}
		
		annotations = AnnotationsUtil.process(handler, TemplateFunction.class, List.of(), Object.class);

		for (var entry : annotations) {
			String name = entry.annotation().value();
            String namespace = entry.annotation().value();
              
			functionMap.put(namespace, name, param -> {
				try {
					return entry.invoke(null);
				} catch (Exception e) {
					throw new RuntimeException("Error calling component: " + name, e);
				}
			});
		}
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
