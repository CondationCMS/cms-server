package	com.condation.cms.modules.system;

/*-
 * #%L
 * cms-auth
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

import com.condation.cms.api.extensions.RegisterShortCodesExtensionPoint;
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.module.CMSRequestContext;
import com.condation.modules.api.annotation.Extension;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author thmar
 */
@Extension(RegisterShortCodesExtensionPoint.class)
public class AuthShortCodes extends RegisterShortCodesExtensionPoint {

	@Override
	public Map<String, Function<Parameter, String>> shortCodes() {
		return Map.of(
				"username", this::getUserName,
				"cms:username", this::getUserName
		);
	}
	
	private String getUserName (Parameter param) {
		return (String)getFeatureValueOrDefault(
				getRequestContext(),
				AuthFeature.class,
				(feature) -> feature.username(),
				"");
	}
	
	private <F extends Feature> Object getFeatureValueOrDefault(CMSRequestContext context,
			Class<F> feature, Function<F, Object> valueFunction, Object defaultValue) {
		if (context.has(feature)) {
			return valueFunction.apply(context.get(feature));
		}
		return defaultValue;
	}
}
