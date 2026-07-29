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
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import com.condation.cms.api.workflow.WFStatusProvider;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Default variant selection with support for preview overrides.
 *
 * @author thorstenmarx
 */
@Slf4j
public class DefaultVariantSelector implements VariantSelector {

	public static final String VARIANT_QUERY_PARAMETER = "variant";
	public static final String CANONICAL_VARIANT_ID = "default";

	@Override
	public VariantSelection select(
			ContentNode canonicalNode,
			List<VariantResolver.Variant> variants,
			RequestContext context
	) {
		if (isManager(context)) {
			return VariantSelection.canonical();
		}

		if (isPreview(context) && context.has(RequestFeature.class)) {
			var request = context.get(RequestFeature.class);
			if (request.hasQueryParameter(VARIANT_QUERY_PARAMETER)) {
				return selectPreviewVariant(canonicalNode, variants, request);
			}
		}

		return selectAutomatically(canonicalNode, variants, context);
	}
    
	private boolean isPreview (RequestContext context) {
		return context.has(IsPreviewFeature.class) 
				&& IsPreviewFeature.Mode.PREVIEW.equals(context.get(IsPreviewFeature.class).mode());
	}

	private boolean isManager(RequestContext context) {
		return context.has(IsPreviewFeature.class)
				&& IsPreviewFeature.Mode.MANAGER.equals(context.get(IsPreviewFeature.class).mode());
	}

	protected VariantSelection selectAutomatically(
			ContentNode canonicalNode,
			List<VariantResolver.Variant> variants,
			RequestContext context
	) {
		var statusProvider = getStatusProvider(context);

		return variants.stream()
				.map(variant -> new ScheduledVariant(
						variant,
						statusProvider.status(variant.node())
				))
				.filter(variant -> variant.status().published())
				.filter(variant -> variant.status().withinSchedule())
				.max(Comparator.comparing(
						variant -> variant.status().publish_date(),
						Comparator.nullsFirst(Comparator.naturalOrder())
				))
				.map(ScheduledVariant::variant)
				.map(VariantSelection::automatic)
				.orElseGet(VariantSelection::canonical);
	}

	private WFStatusProvider getStatusProvider(RequestContext context) {
		if (context.has(WorkflowFeature.class)) {
			return context.get(WorkflowFeature.class)
					.workflow()
					.getStatusProvider();
		}
		return new DefaultWFStatusProvider();
	}

	private VariantSelection selectPreviewVariant(
			ContentNode canonicalNode,
			List<VariantResolver.Variant> variants,
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

	private record ScheduledVariant(
			VariantResolver.Variant variant,
			WFStatusProvider.Status status
	) {
	}
}
