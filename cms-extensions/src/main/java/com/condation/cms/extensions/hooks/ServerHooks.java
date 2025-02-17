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
import com.condation.cms.api.request.RequestContext;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@Experimental
@RequiredArgsConstructor
@FeatureScope(FeatureScope.Scope.REQUEST)
public class ServerHooks implements Feature {
	
	private final RequestContext requestContext;
	
	
	public HttpHandlerWrapper getHttpExtensions () {
		var httpExtensions = new HttpHandlerWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.HTTP_EXTENSION.hook(), Map.of("httpExtensions", httpExtensions));
		
		return httpExtensions;
	}
	
	public HttpHandlerWrapper getHttpRoutes () {
		var httpExtensions = new HttpHandlerWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.HTTP_ROUTE.hook(), Map.of("httpRoutes", httpExtensions));
		
		return httpExtensions;
	}

	public HttpHandlerWrapper getAPIRoutes () {
		var httpExtensions = new HttpHandlerWrapper();
		requestContext.get(HookSystemFeature.class).hookSystem()
				.execute(Hooks.API_ROUTE.hook(), Map.of("apiRoutes", httpExtensions));
		
		return httpExtensions;
	}
}
