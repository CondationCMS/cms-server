package com.condation.cms.extensions.hooks;

/*-
 * #%L
 * cms-extensions
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


import com.condation.cms.api.annotations.Experimental;
import com.condation.cms.api.annotations.FeatureScope;
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@Experimental
@RequiredArgsConstructor
@FeatureScope(FeatureScope.Scope.REQUEST)
public class TemplateHooks implements Feature {
	
	private final RequestContext requestContext;
	
	
	public TemplateSupplierWrapper getTemplateSupplier () {
		var templateSupplier = new TemplateSupplierWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.TEMPLATE_SUPPLIER.hook(), Map.of("suppliers", templateSupplier));
		
		return templateSupplier;
	}
	
	public TemplateFunctionWrapper getTemplateFunctions () {
		var templateFunctions = new TemplateFunctionWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.TEMPLATE_FUNCTION.hook(), Map.of("functions", templateFunctions));
		
		return templateFunctions;
	}
	
	public TemplateComponentsWrapper getComponents (Map<String, Function<Parameter, String>> components) {
		var componentsWrapper = new TemplateComponentsWrapper(components);
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.TEMPLATE_COMPONENT.hook(), Map.of("components", componentsWrapper));
		
		return componentsWrapper;
	}
}
