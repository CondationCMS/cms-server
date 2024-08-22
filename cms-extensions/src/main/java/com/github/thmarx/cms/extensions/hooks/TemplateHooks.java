package com.github.thmarx.cms.extensions.hooks;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.annotations.Experimental;
import com.github.thmarx.cms.api.annotations.FeatureScope;
import com.github.thmarx.cms.api.feature.Feature;
import com.github.thmarx.cms.api.feature.features.HookSystemFeature;
import com.github.thmarx.cms.api.request.RequestContext;
import java.util.Map;
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
				.execute("template/supplier/add", Map.of("suppliers", templateSupplier));
		
		return templateSupplier;
	}
	
	public TemplateFunctionWrapper getTemplateFunctions () {
		var templateFunctions = new TemplateFunctionWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute("template/function/add", Map.of("functions", templateFunctions));
		
		return templateFunctions;
	}
}
