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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.feature.features.RequestFeature;
import com.condation.cms.api.request.RequestContext;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultVariantSelectorTest {

	private final DefaultVariantSelector selector = new DefaultVariantSelector();
	private final ContentNode canonicalNode = node("about.md");
	private final VariantResolver.Variant summer = new VariantResolver.Variant(
			"summer",
			node(".variants/about/summer/about.md")
	);

	@Test
	public void previewCanSelectVariant() {
		var selection = selector.select(
				canonicalNode,
				List.of(summer),
				context(true, "summer")
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.PREVIEW);
		Assertions.assertThat(selection.variant()).contains(summer);
	}

	@Test
	public void previewCanForceCanonicalVariant() {
		var selection = selector.select(
				canonicalNode,
				List.of(summer),
				context(true, DefaultVariantSelector.CANONICAL_VARIANT_ID)
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.CANONICAL);
		Assertions.assertThat(selection.variant()).isEmpty();
	}

	@Test
	public void managerAlwaysUsesCanonicalVariant() {
		var active = scheduledVariant(
				"active",
				Instant.now().minus(Duration.ofDays(1)),
				Instant.now().plus(Duration.ofDays(1)),
				"published"
		);
		var context = context(false, "active");
		context.add(
				IsPreviewFeature.class,
				new IsPreviewFeature(IsPreviewFeature.Mode.MANAGER)
		);

		var selection = selector.select(
				canonicalNode,
				List.of(active),
				context
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.CANONICAL);
		Assertions.assertThat(selection.variant()).isEmpty();
	}

	@Test
	public void invalidPreviewVariantFallsBackToCanonicalVariant() {
		var selection = selector.select(
				canonicalNode,
				List.of(summer),
				context(true, "missing")
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.CANONICAL);
		Assertions.assertThat(selection.variant()).isEmpty();
	}

	@Test
	public void publicRequestCannotOverrideVariant() {
		var selection = selector.select(
				canonicalNode,
				List.of(summer),
				context(false, "summer")
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.CANONICAL);
		Assertions.assertThat(selection.variant()).isEmpty();
	}

	@Test
	public void automaticSelectionUsesPublishedVariantWithinSchedule() {
		var active = scheduledVariant(
				"active",
				Instant.now().minus(Duration.ofDays(1)),
				Instant.now().plus(Duration.ofDays(1)),
				"published"
		);

		var selection = selector.select(
				canonicalNode,
				List.of(active),
				context(false, null)
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.AUTOMATIC);
		Assertions.assertThat(selection.variant()).contains(active);
	}

	@Test
	public void automaticSelectionIgnoresDraftAndVariantsOutsideSchedule() {
		var draft = scheduledVariant(
				"draft",
				Instant.now().minus(Duration.ofDays(1)),
				Instant.now().plus(Duration.ofDays(1)),
				"draft"
		);
		var future = scheduledVariant(
				"future",
				Instant.now().plus(Duration.ofDays(1)),
				Instant.now().plus(Duration.ofDays(2)),
				"published"
		);
		var expired = scheduledVariant(
				"expired",
				Instant.now().minus(Duration.ofDays(2)),
				Instant.now().minus(Duration.ofDays(1)),
				"published"
		);

		var selection = selector.select(
				canonicalNode,
				List.of(draft, future, expired),
				context(false, null)
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.CANONICAL);
		Assertions.assertThat(selection.variant()).isEmpty();
	}

	@Test
	public void newestActiveVariantWins() {
		var older = scheduledVariant(
				"older",
				Instant.now().minus(Duration.ofDays(2)),
				Instant.now().plus(Duration.ofDays(1)),
				"published"
		);
		var newer = scheduledVariant(
				"newer",
				Instant.now().minus(Duration.ofDays(1)),
				Instant.now().plus(Duration.ofDays(1)),
				"published"
		);

		var selection = selector.select(
				canonicalNode,
				List.of(newer, older),
				context(false, null)
		);

		Assertions.assertThat(selection.variant()).contains(newer);
	}

	private RequestContext context(boolean preview, String variantId) {
		var context = new RequestContext();
		var queryParameters = variantId == null
				? Map.<String, List<String>>of()
				: Map.of("variant", List.of(variantId));
		context.add(
				RequestFeature.class,
				new RequestFeature("", "/", queryParameters, null)
		);
		if (preview) {
			context.add(IsPreviewFeature.class, new IsPreviewFeature());
		}
		return context;
	}

	private ContentNode node(String path) {
		return new ContentNode(path, "/" + path, path, Map.of());
	}

	private VariantResolver.Variant scheduledVariant(
			String id,
			Instant publishDate,
			Instant unpublishDate,
			String status
	) {
		Map<String, Object> data = new HashMap<>();
		data.put(Constants.MetaFields.STATUS, status);
		data.put(Constants.MetaFields.PUBLISH_DATE, Date.from(publishDate));
		data.put(Constants.MetaFields.UNPUBLISH_DATE, Date.from(unpublishDate));
		return new VariantResolver.Variant(
				id,
				new ContentNode(
						".variants/about/" + id + "/about.md",
						"/.variants/about/" + id + "/about",
						"about.md",
						data
				)
		);
	}
}
