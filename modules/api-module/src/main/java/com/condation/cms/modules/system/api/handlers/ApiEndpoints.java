package com.condation.cms.modules.system.api.handlers;

/*-
 * #%L
 * cms-system-modules
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
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.modules.system.api.handlers.v1.ContentHandler;
import com.condation.cms.api.extensions.http.APIHandlerExtensionPoint;
import com.condation.cms.api.extensions.http.PathMapping;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.modules.system.api.services.ContentService;
import com.condation.cms.modules.system.api.handlers.v1.NavigationHandler;
import com.condation.cms.modules.system.api.handlers.v1.QueryHandler;
import com.condation.modules.api.annotation.Extension;
import java.util.HashSet;
import java.util.List;
import org.eclipse.jetty.http.pathmap.PathSpec;

/**
 *
 * @author thmar
 */
@Experimental
@Extension(APIHandlerExtensionPoint.class)
public class ApiEndpoints extends APIHandlerExtensionPoint {

	@Override
	public PathMapping getMapping() {
		var mapping = new PathMapping();
		
		var siteProperties = getContext().get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		var whitelist = siteProperties.getOrDefault("api.whitelist", List.of(""));
		
		mapping.add(PathSpec.from("/v1/content/*"), 
				"GET", 
				new ContentHandler(new ContentService(
						getContext().get(DBFeature.class).db(),
						new HashSet<>(whitelist)
				))
		);
		
		mapping.add(PathSpec.from("/v1/navigation/*"), 
				"GET", 
				new NavigationHandler(getContext().get(DBFeature.class).db(), getRequestContext())
		);
		
		mapping.add(PathSpec.from("/v1/query"),
				"POST",
				new QueryHandler(getContext().get(DBFeature.class).db())
		);

		return mapping;
	}
	
}
