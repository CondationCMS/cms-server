package com.condation.cms.templates;

/*-
 * #%L
 * cms-templates
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
import com.condation.cms.api.extensions.RegisterTemplateFunctionExtensionPoint;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.extensions.TemplateFunctionExtension;
import com.condation.cms.extensions.hooks.TemplateHooks;
import com.condation.cms.templates.components.TemplateComponents;
import com.condation.cms.templates.components.TemplateFunctions;
import com.condation.cms.templates.functions.TemplateFunction;
import com.condation.cms.templates.tags.component.EndComponentTag;
import com.condation.cms.templates.tags.component.ComponentTag;
import com.condation.modules.api.ModuleManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
public record DynamicConfiguration(TemplateComponents templateComponents, Map<String, Component> components, RequestContext requestContext) {

	public static final DynamicConfiguration EMPTY = new DynamicConfiguration(
			new TemplateComponents(),
			Collections.emptyMap(),
			null
	);

	public DynamicConfiguration    {
		for (var tag : templateComponents.getComponentNames()) {
			var openTag = new ComponentTag(tag, templateComponents);
			var closeTag = new EndComponentTag(tag);

			components.put(openTag.getName(), openTag);
			components.put(closeTag.getName(), closeTag);
		}
	}

	public DynamicConfiguration(TemplateComponents templateComponents, RequestContext requestContext) {
		this(templateComponents, new HashMap<>(), requestContext);
	}

	public boolean hasComponent(String name) {
		return components.containsKey(name);
	}

	public Optional<Component> getComponent(String name) {
		return Optional.ofNullable(components.get(name));
	}

	public List<TemplateFunction> templateFunctions() {
		if (requestContext == null) {
			return Collections.emptyList();
		}
		var templateFunctions = requestContext.get(TemplateHooks.class).getTemplateFunctions();
		var tfs = new ArrayList<TemplateFunction>();
		templateFunctions.getRegisterTemplateFunctions().stream()
				.map(function -> new FunctionWrapper(function.name(), function.function(), requestContext))
				.forEach(tfs::add);
		
		var injector = requestContext.get(InjectorFeature.class).injector();
		
		var templateFunctionsModel = new TemplateFunctions();
		injector.getInstance(ModuleManager.class)
				.extensions(RegisterTemplateFunctionExtensionPoint.class)
				.forEach(extension -> {
					templateFunctionsModel.register(extension.functions());
					templateFunctionsModel.register(extension.functionDefinitions());
				});
		
		templateFunctionsModel.getFunctionMap().names().forEach(name -> {
			tfs.add(
					new FunctionWrapper(name, templateFunctionsModel.getFunctionMap().get(name), requestContext)
			);
		});
		
		return tfs;
	}

	@RequiredArgsConstructor
	public static class FunctionWrapper implements TemplateFunction {

		private final String name;
		private final Function<Parameter, ?> function;
		private final RequestContext requestContext;
		
		@Override
		public Object invoke(Object... params) {
			Parameter parameter = null;
				if (params.length == 1 && params[0] instanceof Parameter) {
					parameter = (Parameter)params[0];
				} else if (params.length == 1 && params[0] instanceof Map) {
					parameter = new Parameter((Map<String, Object>)params[0], requestContext);
				} else {
					parameter = new Parameter();
				}
				return function.apply(parameter);
		}

		@Override
		public String name() {
			return name;
		}
		
	}
}
