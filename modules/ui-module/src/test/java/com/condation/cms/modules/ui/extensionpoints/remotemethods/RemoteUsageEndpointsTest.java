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

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.usage.*;
import com.google.inject.Guice;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteUsageEndpointsTest {
    @Test void incomingIncludesSiteCoverageProblemsAndUsesCurrentSiteIdentity() throws Exception {
        var index = mock(UsageIndex.class);
        var target = new UsageResource("de", UsageResource.Kind.MEDIA, "images/logo.svg");
        var usage = new Usage(new UsageResource("de", UsageResource.Kind.CONTENT, "index.md"), target,
                "metadata.logo", Usage.Origin.CONTENT_TYPE, "images/logo.svg", "Home", "draft", Usage.TargetStatus.EXISTS);
        when(index.incoming(target)).thenReturn(List.of(usage));
        var problem = new UsageProblem("de", "other.md", "Missing content type");
        when(index.problems()).thenReturn(List.of(problem));
        var context = context(index);
        var endpoint = new RemoteUsageEndpoints();
        var result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, context)
                .call(() -> endpoint.incoming(Map.of("kind", "media", "path", "/images/logo.svg", "site", "ignored")));
        assertThat((Map<String, ?>) result).containsKey("items");
        assertThat(((Map<?, ?>) result).get("items")).isEqualTo(List.of(usage));
        assertThat(((Map<?, ?>) result).get("problems")).isEqualTo(List.of(problem));
        assertThat(((Map<?, ?>) result).get("scope")).isEqualTo("editorial");
        verify(index).incoming(target);
    }

    @Test void rejectsInvalidKindsAndPathsEscapingSiteRoot() throws Exception {
        var context = context(mock(UsageIndex.class));
        var endpoint = new RemoteUsageEndpoints();
        ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, context).run(() -> {
            assertThatThrownBy(() -> endpoint.outgoing(Map.of("kind", "content", "path", "../../outside.md")))
                    .isInstanceOf(RPCException.class);
            assertThatThrownBy(() -> endpoint.outgoing(Map.of("kind", "template", "path", "page.html")))
                    .isInstanceOf(RPCException.class);
        });
    }

    @Test void sharedCollectionUsesConfiguredOwner() throws Exception {
        var index = mock(UsageIndex.class);
        var context = context(index);
        var configuration = mock(com.condation.cms.api.configuration.Configuration.class);
        when(configuration.get(com.condation.cms.api.configuration.configs.CollectionConfiguration.class))
                .thenReturn(new com.condation.cms.api.configuration.configs.CollectionConfiguration(Map.of(
                        "news", new com.condation.cms.api.configuration.configs.CollectionDefinition("news", "en", null))));
        context.add(com.condation.cms.api.feature.features.ConfigurationFeature.class,
                new com.condation.cms.api.feature.features.ConfigurationFeature(configuration));
        ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, context).call(() ->
                new RemoteUsageEndpoints().incoming(Map.of("kind", "COLLECTION_ITEM", "path", "news/example.md")));
        verify(index).incoming(new UsageResource("en", UsageResource.Kind.COLLECTION_ITEM, "news/example.md"));
    }

    private RequestContext context(UsageIndex index) {
        var properties = mock(SiteProperties.class);
        when(properties.id()).thenReturn("de");
        var context = new RequestContext();
        context.add(SitePropertiesFeature.class, new SitePropertiesFeature(properties));
        context.add(InjectorFeature.class, new InjectorFeature(Guice.createInjector(binder -> binder.bind(UsageIndex.class).toInstance(index))));
        return context;
    }
}
