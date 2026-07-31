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

import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.extensions.VariantSelectorExtensionPoint;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.api.variants.VariantSelection;
import com.condation.cms.api.variants.VariantSelector;
import com.condation.modules.api.ModuleManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies CMS-owned manager and preview rules before delegating public
 * requests to the selector configured for the canonical page.
 */
@Slf4j
public class ConfigurableVariantSelector implements VariantSelector {

	public static final String DATE_RANGE_LABEL = "Date range";
	public static final String VARIANT_QUERY_PARAMETER = "variant";
	public static final String CANONICAL_VARIANT_ID = "default";

	private final DefaultVariantSelector dateRangeSelector;
	private final VariantSelectorConfigurationRepository configurationRepository;
	private final ModuleManager moduleManager;

	public ConfigurableVariantSelector(
			DefaultVariantSelector dateRangeSelector,
			VariantSelectorConfigurationRepository configurationRepository,
			ModuleManager moduleManager
	) {
		this.dateRangeSelector = dateRangeSelector;
		this.configurationRepository = configurationRepository;
		this.moduleManager = moduleManager;
	}

	@Override
	public VariantSelection select(
			ContentNode canonicalNode,
			List<Variant> variants,
			RequestContext context
	) {
		if (hasExplicitPreviewSelection(context)) {
			return selectPreviewVariant(canonicalNode, variants, context.get(RequestFeature.class));
		}
		if (isManager(context)) {
			return VariantSelection.canonical();
		}

		var selectorId = configurationRepository.getSelectorId(canonicalNode);
		var selector = selectors().get(selectorId);
		if (selector == null) {
			log.warn(
					"Configured variant selector '{}' is unavailable for '{}'; using '{}'",
					selectorId,
					canonicalNode.path(),
					VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID
			);
			selector = dateRangeSelector;
		}

		try {
			return selector.select(canonicalNode, variants, context);
		} catch (RuntimeException exception) {
			log.error(
					"Variant selector '{}' failed for '{}'; using date-range fallback",
					selectorId,
					canonicalNode.path(),
					exception
			);
			return dateRangeSelector.select(canonicalNode, variants, context);
		}
	}

	public Map<String, SelectorDescriptor> availableSelectors() {
		Map<String, SelectorDescriptor> descriptors = new LinkedHashMap<>();
		descriptors.put(
				VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID,
				new SelectorDescriptor(
						VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID,
						DATE_RANGE_LABEL
				)
		);
		moduleManager.extensions(VariantSelectorExtensionPoint.class).forEach(extension ->
				descriptors.putIfAbsent(
						extension.id(),
						new SelectorDescriptor(extension.id(), extension.label())
				)
		);
		return Map.copyOf(descriptors);
	}

	public boolean hasSelector(String selectorId) {
		return availableSelectors().containsKey(selectorId);
	}

	private Map<String, VariantSelector> selectors() {
		Map<String, VariantSelector> selectors = new LinkedHashMap<>();
		selectors.put(VariantSelectorConfigurationRepository.DEFAULT_SELECTOR_ID, dateRangeSelector);
		moduleManager.extensions(VariantSelectorExtensionPoint.class).forEach(extension ->
				selectors.putIfAbsent(extension.id(), extension)
		);
		return selectors;
	}

	private boolean isManager(RequestContext context) {
		return context.has(IsPreviewFeature.class)
				&& IsPreviewFeature.Mode.MANAGER.equals(context.get(IsPreviewFeature.class).mode());
	}

	private boolean hasExplicitPreviewSelection(RequestContext context) {
		return context.has(IsPreviewFeature.class)
				&& (IsPreviewFeature.Mode.PREVIEW.equals(context.get(IsPreviewFeature.class).mode())
						|| IsPreviewFeature.Mode.MANAGER.equals(context.get(IsPreviewFeature.class).mode()))
				&& context.has(RequestFeature.class)
				&& context.get(RequestFeature.class)
						.hasQueryParameter(VARIANT_QUERY_PARAMETER);
	}

	private VariantSelection selectPreviewVariant(
			ContentNode canonicalNode,
			List<Variant> variants,
			RequestFeature request
	) {
		var variantId = request.getQueryParameter(VARIANT_QUERY_PARAMETER, "").trim();
		if (variantId.isBlank() || CANONICAL_VARIANT_ID.equalsIgnoreCase(variantId)) {
			return VariantSelection.canonical();
		}

		return variants.stream()
				.filter(variant -> variant.id().equals(variantId))
				.findFirst()
				.map(VariantSelection::preview)
				.orElseGet(() -> {
					log.warn(
							"Requested preview variant '{}' does not exist for '{}'",
							variantId,
							canonicalNode.path()
					);
					return VariantSelection.canonical();
				});
	}

	public record SelectorDescriptor(String id, String label) {
	}
}
