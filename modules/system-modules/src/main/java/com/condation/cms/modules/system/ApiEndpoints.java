package com.condation.cms.modules.system;

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

import com.condation.cms.api.extensions.http.APIHandlerExtensionPoint;
import com.condation.cms.api.extensions.http.PathMapping;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.modules.api.annotation.Extension;
import org.eclipse.jetty.http.pathmap.PathSpec;

/**
 *
 * @author thmar
 */
@Extension(APIHandlerExtensionPoint.class)
public class ApiEndpoints extends APIHandlerExtensionPoint {

	@Override
	public PathMapping getMapping() {
		var mapping = new PathMapping();
		
		mapping.add(
				PathSpec.from("/content/*"), 
				"GET", 
				new ApiContentHandler(getContext().get(DBFeature.class).db())
		);
		
		return mapping;
	}
	
}
