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
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.variants.Variant;
import com.condation.cms.api.variants.VariantSelection;
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
				context()
		);

		Assertions.assertThat(selection.source()).isEqualTo(VariantSelection.Source.AUTOMATIC);
		Assertions.assertThat(selection.variant()).contains(active);
	}

	@Test
	public void automaticSelectionSupportsPublishedVariantWithoutSchedule() {
		var unscheduled = new Variant(
				"unscheduled",
				new ContentNode(
						".variants/about/unscheduled/about.md",
						"/.variants/about/unscheduled/about",
						"about.md",
						Map.of(Constants.MetaFields.STATUS, "published")
				)
		);

		var selection = selector.select(
				canonicalNode,
				List.of(unscheduled),
				context()
		);

		Assertions.assertThat(selection.variant()).contains(unscheduled);
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
				context()
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
				context()
		);

		Assertions.assertThat(selection.variant()).contains(newer);
	}

	@Test
	public void variantsWithSameScheduleUseIdAsStableTieBreaker() {
		var publishDate = Instant.now().minus(Duration.ofDays(1));
		var unpublishDate = Instant.now().plus(Duration.ofDays(1));
		var alpha = scheduledVariant("alpha", publishDate, unpublishDate, "published");
		var beta = scheduledVariant("beta", publishDate, unpublishDate, "published");

		var firstOrder = selector.select(
				canonicalNode,
				List.of(beta, alpha),
				context()
		);
		var secondOrder = selector.select(
				canonicalNode,
				List.of(alpha, beta),
				context()
		);

		Assertions.assertThat(firstOrder.variant()).contains(beta);
		Assertions.assertThat(secondOrder.variant()).contains(beta);
	}

	private RequestContext context() {
		return new RequestContext();
	}

	private ContentNode node(String path) {
		return new ContentNode(path, "/" + path, path, Map.of());
	}

	private Variant scheduledVariant(
			String id,
			Instant publishDate,
			Instant unpublishDate,
			String status
	) {
		Map<String, Object> data = new HashMap<>();
		data.put(Constants.MetaFields.STATUS, status);
		data.put(Constants.MetaFields.PUBLISH_DATE, Date.from(publishDate));
		data.put(Constants.MetaFields.UNPUBLISH_DATE, Date.from(unpublishDate));
		return new Variant(
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
