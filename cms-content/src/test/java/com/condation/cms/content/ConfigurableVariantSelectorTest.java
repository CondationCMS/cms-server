package com.condation.cms.content;

/*-
 * #%L
 * CMS Content
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.extensions.VariantSelectorExtensionPoint;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.api.variants.VariantSelection;
import com.condation.modules.api.ModuleManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigurableVariantSelectorTest {

	private final ContentNode canonical = new ContentNode("about.md", "/about", "about.md", Map.of());
	private final Variant summer = new Variant(
			"summer",
			new ContentNode(".variants/about/summer/about.md", "/summer", "about.md", Map.of())
	);
	private final List<Variant> variants = List.of(summer);
	private DefaultVariantSelector dateRange;
	private VariantSelectorConfigurationRepository configuration;
	private VariantSelectorExtensionPoint extension;
	private ConfigurableVariantSelector selector;

	@BeforeEach
	void setUp() {
		dateRange = mock(DefaultVariantSelector.class);
		configuration = mock(VariantSelectorConfigurationRepository.class);
		extension = mock(VariantSelectorExtensionPoint.class);
		var moduleManager = mock(ModuleManager.class);
		when(extension.id()).thenReturn("audience");
		when(extension.label()).thenReturn("Audience");
		when(moduleManager.extensions(VariantSelectorExtensionPoint.class)).thenReturn(List.of(extension));
		selector = new ConfigurableVariantSelector(dateRange, configuration, moduleManager);
	}

	@Test
	void publicRequestsUseConfiguredModuleSelector() {
		var context = mock(RequestContext.class);
		var expected = VariantSelection.canonical();
		when(configuration.getSelectorId(canonical)).thenReturn("audience");
		when(extension.select(canonical, variants, context)).thenReturn(expected);

		assertThat(selector.select(canonical, variants, context)).isSameAs(expected);
		verify(extension).select(canonical, variants, context);
	}

	@Test
	void managerRequestsAlwaysUseCentralSelectionLogic() {
		var context = context(IsPreviewFeature.Mode.MANAGER, null);

		assertThat(selector.select(canonical, variants, context))
				.isEqualTo(VariantSelection.canonical());
		verify(configuration, never()).getSelectorId(canonical);
		verify(dateRange, never()).select(canonical, variants, context);
		verify(extension, never()).select(canonical, variants, context);
	}

	@Test
	void explicitPreviewSelectsVariantWithoutInvokingASelector() {
		var context = context(IsPreviewFeature.Mode.PREVIEW, "summer");

		assertThat(selector.select(canonical, variants, context))
				.isEqualTo(VariantSelection.preview(summer));
		verify(configuration, never()).getSelectorId(canonical);
		verify(dateRange, never()).select(canonical, variants, context);
		verify(extension, never()).select(canonical, variants, context);
	}

	@Test
	void explicitManagerPreviewSelectsVariantWithoutInvokingASelector() {
		var context = context(IsPreviewFeature.Mode.MANAGER, "summer");

		assertThat(selector.select(canonical, variants, context))
				.isEqualTo(VariantSelection.preview(summer));
		verify(configuration, never()).getSelectorId(canonical);
		verify(dateRange, never()).select(canonical, variants, context);
		verify(extension, never()).select(canonical, variants, context);
	}

	@Test
	void explicitPreviewCanForceCanonicalContent() {
		var context = context(
				IsPreviewFeature.Mode.PREVIEW,
				ConfigurableVariantSelector.CANONICAL_VARIANT_ID
		);

		assertThat(selector.select(canonical, variants, context))
				.isEqualTo(VariantSelection.canonical());
		verify(dateRange, never()).select(canonical, variants, context);
		verify(extension, never()).select(canonical, variants, context);
	}

	@Test
	void exposesBuiltInAndModuleSelectors() {
		assertThat(selector.availableSelectors())
				.containsKeys(VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID, "audience");
	}

	private RequestContext context(IsPreviewFeature.Mode mode, String variantId) {
		var context = mock(RequestContext.class);
		var request = mock(RequestFeature.class);
		when(context.has(IsPreviewFeature.class)).thenReturn(true);
		when(context.get(IsPreviewFeature.class)).thenReturn(new IsPreviewFeature(mode));
		when(context.has(RequestFeature.class)).thenReturn(true);
		when(context.get(RequestFeature.class)).thenReturn(request);
		when(request.hasQueryParameter(ConfigurableVariantSelector.VARIANT_QUERY_PARAMETER))
				.thenReturn(variantId != null);
		when(request.getQueryParameter(ConfigurableVariantSelector.VARIANT_QUERY_PARAMETER, ""))
				.thenReturn(variantId == null ? "" : variantId);
		return context;
	}
}
