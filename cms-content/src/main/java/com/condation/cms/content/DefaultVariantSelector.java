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
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.api.variants.VariantSelection;
import com.condation.cms.api.variants.VariantSelector;
import com.condation.cms.api.workflow.DefaultWFStatusProvider;
import com.condation.cms.api.workflow.WFStatusProvider;
import java.util.Comparator;
import java.util.List;

/**
 * Selects the newest published variant within its configured date range.
 *
 * @author thorstenmarx
 */
public class DefaultVariantSelector implements VariantSelector {

	@Override
	public VariantSelection select(
			ContentNode canonicalNode,
			List<Variant> variants,
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
				.max(Comparator.<ScheduledVariant, java.util.Date>comparing(
						variant -> variant.status().publish_date(),
						Comparator.nullsFirst(Comparator.naturalOrder())
				).thenComparing(variant -> variant.variant().id()))
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

	private record ScheduledVariant(
			Variant variant,
			WFStatusProvider.Status status
	) {
	}
}
