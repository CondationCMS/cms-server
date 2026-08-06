package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * CMS FileSystem
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
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.api.utils.DateRange;
import com.condation.cms.api.workflow.WFStatusProvider;
import com.condation.cms.api.workflow.WFStatusQueryProvider;
import com.condation.cms.api.workflow.WorkflowInstance;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LuceneQueryPagingTest {

	@TempDir
	Path tempDirectory;

	@Test
	void defaultWorkflowFiltersInLuceneAndLoadsOnlyRequestedNodes() throws Exception {
		try (var metadata = new CountingPersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("without-rank.md", page(null, "published"), LocalDate.now());
			metadata.addFile("legacy.md", legacyPage(1), LocalDate.now());
			metadata.addFile("published.md", page(2, "published"), LocalDate.now());
			metadata.addFile("draft.md", page(0, "draft"), LocalDate.now());
			metadata.addFile(
					"future.md",
					scheduledPage(3, Date.from(Instant.now().plusSeconds(3_600)), null),
					LocalDate.now());
			metadata.addFile(
					"expired.md",
					scheduledPage(4, null, Date.from(Instant.now().minusSeconds(3_600))),
					LocalDate.now());
			metadata.addFile("article.slot.md", page(-1, "published"), LocalDate.now());
			metadata.addFile(".private/page.md", page(-2, "published"), LocalDate.now());

			metadata.resetByPathCalls();
			var firstPage = metadata.query((node, excerpt) -> node)
					.orderby("rank").asc()
					.page(1, 1);

			Assertions.assertThat(firstPage.getTotalItems()).isEqualTo(3);
			Assertions.assertThat(firstPage.getTotalPages()).isEqualTo(3);
			Assertions.assertThat(firstPage.getItems())
					.singleElement()
					.extracting(ContentNode::uri)
					.isEqualTo("without-rank.md");
			Assertions.assertThat(metadata.byPathCalls()).isEqualTo(1);

			metadata.resetByPathCalls();
			var secondPage = metadata.query((node, excerpt) -> node)
					.orderby("rank").asc()
					.page(2, 1);

			Assertions.assertThat(secondPage.getItems())
					.singleElement()
					.extracting(ContentNode::uri)
					.isEqualTo("legacy.md");
			Assertions.assertThat(metadata.byPathCalls()).isEqualTo(1);
		}
	}

	@Test
	void customWorkflowDeterminesVisibilityAndKeepsExactTotals() throws Exception {
		try (var metadata = new CountingPersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("custom-visible.md", workflowPage(1, "draft", "approved"), LocalDate.now());
			metadata.addFile("custom-hidden.md", workflowPage(2, "published", "blocked"), LocalDate.now());
			metadata.addFile("also-visible.md", workflowPage(3, "published", "approved"), LocalDate.now());

			var requestContext = new RequestContext();
			requestContext.add(
					WorkflowFeature.class,
					new WorkflowFeature(new WorkflowInstance("approval", "Approval", approvalStatusProvider())));

			metadata.resetByPathCalls();
			var result = ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext)
					.call(() -> metadata.query((node, excerpt) -> node)
							.orderby("rank").asc()
							.page(1, 1));

			Assertions.assertThat(result.getTotalItems()).isEqualTo(2);
			Assertions.assertThat(result.getTotalPages()).isEqualTo(2);
			Assertions.assertThat(result.getItems())
					.singleElement()
					.extracting(ContentNode::uri)
					.isEqualTo("custom-visible.md");
			Assertions.assertThat(metadata.byPathCalls()).isEqualTo(1);
		}
	}

	@Test
	void mixedMetadataTypesUseSeparateDocValuesAndFallBackSafely() throws Exception {
		try (var metadata = new PersistentMetaData(tempDirectory)) {
			metadata.open();
			metadata.addFile("number.md", pageWithSortValue(1), LocalDate.now());
			metadata.addFile("string.md", pageWithSortValue("one"), LocalDate.now());

			var result = metadata.query((node, excerpt) -> node)
					.orderby("sort_value").asc()
					.page(1, 10);

			Assertions.assertThat(result.getTotalItems()).isEqualTo(2);
			Assertions.assertThat(result.getItems())
					.extracting(ContentNode::uri)
					.containsExactlyInAnyOrder("number.md", "string.md");
		}
	}

	private static Map<String, Object> page(Object rank, String status) {
		var data = new HashMap<String, Object>();
		data.put(Constants.MetaFields.STATUS, status);
		if (rank != null) {
			data.put("rank", rank);
		}
		return data;
	}

	private static Map<String, Object> legacyPage(int rank) {
		var data = new HashMap<String, Object>();
		data.put(Constants.MetaFields.PUBLISHED, true);
		data.put("rank", rank);
		return data;
	}

	private static Map<String, Object> scheduledPage(int rank, Date publishDate, Date unpublishDate) {
		var data = page(rank, "published");
		if (publishDate != null) {
			data.put(Constants.MetaFields.PUBLISH_DATE, publishDate);
		}
		if (unpublishDate != null) {
			data.put(Constants.MetaFields.UNPUBLISH_DATE, unpublishDate);
		}
		return data;
	}

	private static Map<String, Object> workflowPage(int rank, String status, String approval) {
		var data = page(rank, status);
		data.put("approval", approval);
		return data;
	}

	private static Map<String, Object> pageWithSortValue(Object value) {
		var data = page(null, "published");
		data.put("sort_value", value);
		return data;
	}

	private static WFStatusProvider approvalStatusProvider() {
		return new WFStatusQueryProvider() {
			@Override
			public boolean isPublished(ContentNode node) {
				return "approved".equals(node.data().get("approval"));
			}

			@Override
			public Status status(ContentNode node) {
				var publishDate = (Date) node.data().get(Constants.MetaFields.PUBLISH_DATE);
				var unpublishDate = (Date) node.data().get(Constants.MetaFields.UNPUBLISH_DATE);
				return new Status(
						isPublished(node),
						publishDate,
						unpublishDate,
						DateRange.isNowWithin(publishDate, unpublishDate),
						(String) node.data().get("approval"));
			}

			@Override
			public String newNodeStatus() {
				return "blocked";
			}

			@Override
			public <T> Optional<ContentQuery<T>> published(ContentQuery<T> query) {
				return Optional.of(query.where("approval", "=", "approved"));
			}

			@Override
			public <T> ContentQuery<T> unpublished(ContentQuery<T> query) {
				return query.where("approval", "!=", "approved");
			}
		};
	}

	private static final class CountingPersistentMetaData extends PersistentMetaData {

		private final AtomicInteger byPathCalls = new AtomicInteger();

		private CountingPersistentMetaData(Path hostPath) {
			super(hostPath);
		}

		@Override
		public Optional<ContentNode> byPath(String path) {
			byPathCalls.incrementAndGet();
			return super.byPath(path);
		}

		private void resetByPathCalls() {
			byPathCalls.set(0);
		}

		private int byPathCalls() {
			return byPathCalls.get();
		}
	}
}
