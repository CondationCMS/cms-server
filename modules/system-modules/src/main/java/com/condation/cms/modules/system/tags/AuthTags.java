package	com.condation.cms.modules.system.tags;

/*-
 * #%L
 * CMS System Modules
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

import com.condation.cms.api.annotations.Tag;
import com.condation.cms.api.extensions.RegisterTagsExtensionPoint;
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.modules.api.annotation.Extension;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author thmar
 */
@Extension(value = RegisterTagsExtensionPoint.class, cached = Extension.Caching.TRUE)
public class AuthTags extends RegisterTagsExtensionPoint {

    @Override
    public List<Object> tagDefinitions() {
        return List.of(this);
    }
    
    @Tag(value = "username", namespace = "cms")
    public String username (Parameter params) {
        return getUserName(params);
    }
    
	
	private String getUserName (Parameter param) {
		return (String)getFeatureValueOrDefault(
				getRequestContext(),
				AuthFeature.class,
				(feature) -> feature.username(),
				"");
	}
	
	private <F extends Feature> Object getFeatureValueOrDefault(RequestContext context,
			Class<F> feature, Function<F, Object> valueFunction, Object defaultValue) {
		if (context.has(feature)) {
			return valueFunction.apply(context.get(feature));
		}
		return defaultValue;
	}
}
