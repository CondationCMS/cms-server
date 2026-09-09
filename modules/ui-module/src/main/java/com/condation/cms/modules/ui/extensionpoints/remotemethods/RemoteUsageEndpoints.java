package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.usage.UsageIndex;
import com.condation.cms.api.usage.UsageResource;
import com.condation.modules.api.annotation.Extension;
import java.util.Locale;
import java.util.Map;

/** Read-only manager access to editorial usages of resources in the current site. */
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteUsageEndpoints extends AbstractRemoteMethodeExtension {
    @RemoteMethod(name = "usage.incoming", permissions = {Permissions.CONTENT_EDIT})
    public Object incoming(Map<String, Object> parameters) throws RPCException {
        return Map.of("items", index().incoming(resource(parameters)), "problems", index().problems(), "scope", "editorial");
    }

    @RemoteMethod(name = "usage.outgoing", permissions = {Permissions.CONTENT_EDIT})
    public Object outgoing(Map<String, Object> parameters) throws RPCException {
        return Map.of("items", index().outgoing(resource(parameters)), "problems", problems(parameters), "scope", "editorial");
    }

    @RemoteMethod(name = "usage.problems", permissions = {Permissions.CONTENT_EDIT})
    public Object problems(Map<String, Object> parameters) {
        return index().problems().stream().filter(problem -> problem.site().equals(site())).toList();
    }

    private UsageResource resource(Map<String, Object> parameters) throws RPCException {
        if (!(parameters.get("kind") instanceof String) || !(parameters.get("path") instanceof String)) {
            throw new RPCException(0, "kind and site-local path are required");
        }
        try {
            var resource = new UsageResource(site(), UsageResource.Kind.valueOf(
                    ((String) parameters.get("kind")).toUpperCase(Locale.ROOT)), (String) parameters.get("path"));
            if (resource.kind() == UsageResource.Kind.COLLECTION_ITEM
                    && getRequestContext().has(ConfigurationFeature.class)) {
                var configuration = getRequestContext().get(ConfigurationFeature.class)
                        .configuration().get(CollectionConfiguration.class);
                if (configuration != null) {
                    var collection = resource.path().split("/", 2)[0];
                    var owner = configuration.collection(collection)
                            .flatMap(definition -> definition.sourceSite()).orElse(site());
                    return new UsageResource(owner, resource.kind(), resource.path());
                }
            }
            return resource;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new RPCException(0, "kind and site-local path are required");
        }
    }

    private String site() { return getRequestContext().get(SitePropertiesFeature.class).siteProperties().id(); }
    private UsageIndex index() { return getRequestContext().get(InjectorFeature.class).injector().getInstance(UsageIndex.class); }
}
