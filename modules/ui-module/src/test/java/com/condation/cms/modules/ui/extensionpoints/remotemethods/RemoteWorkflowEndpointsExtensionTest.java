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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.VariantSearchMode;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.workflow.WFStatusQueryProvider;
import com.condation.cms.api.workflow.Workflow;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RemoteWorkflowEndpointsExtensionTest {

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private DB db;

	@Mock
	private DBFileSystem fileSystem;

	@Mock
	private ReadOnlyFile contentBase;

	@Mock
	private ReadOnlyFile contentFile;

	private RemoteWorkflowEndpointsExtension endpoints;

	@BeforeEach
	void setUp() {
		endpoints = new RemoteWorkflowEndpointsExtension();
		endpoints.setContext(moduleContext);
		when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		when(db.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.contentBase()).thenReturn(contentBase);
		lenient().when(contentBase.resolve("missing.md")).thenReturn(contentFile);
		lenient().when(contentFile.exists()).thenReturn(false);
	}

	@Test
	void nodeStatus_returnsResultWithoutStatus_whenContentNodeNotFound() throws RPCException {
		Map<String, Object> params = Map.of("uri", "missing.md");

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) endpoints.nodeStatus(params);

		assertThat(result).doesNotContainKey("status");
		assertThat(result).doesNotContainKey("error");
	}

	@Test
	void getTransitions_returnsEmptyTransitions_whenContentNodeNotFound() throws RPCException {
		Map<String, Object> params = Map.of("uri", "missing.md");

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) endpoints.getTransitions(params);

		assertThat(result).containsEntry("transitions", java.util.List.of());
		assertThat(result).doesNotContainKey("error");
	}

	@Test
	void transit_throwsRPCException_whenContentNodeNotFound() {
		Map<String, Object> params = Map.of("uri", "missing.md", "transitionId", "publish");

		assertThatThrownBy(() -> endpoints.transit(params))
				.isInstanceOf(RPCException.class)
				.satisfies(ex -> assertThat(((RPCException) ex).getCode()).isEqualTo(404));
	}

	@Test
	void unpublishedPages_delegatesFilteringAndPaginationToWorkflowProvider() throws RPCException {
		Content content = mock(Content.class);
		@SuppressWarnings("unchecked")
		ContentQuery<ContentNode> query = mock(ContentQuery.class);
		WFStatusQueryProvider statusProvider = mock(WFStatusQueryProvider.class);
		Workflow workflow = mock(Workflow.class);
		Page<ContentNode> emptyPage = new Page<>(0, 5, 0, 1, List.of());

		when(db.getContent()).thenReturn(content);
		doReturn(query).when(content).query(any());
		when(query.variants(VariantSearchMode.ORIGINAL)).thenReturn(query);
		when(statusProvider.unpublished(query)).thenReturn(query);
		when(query.page(1, 5)).thenReturn(emptyPage);
		when(workflow.getStatusProvider()).thenReturn(statusProvider);
		when(moduleContext.get(WorkflowFeature.class)).thenReturn(new WorkflowFeature(workflow));

		@SuppressWarnings("unchecked")
		Page<Object> result = (Page<Object>) endpoints.unpublishedPages(Map.of("page", 1, "size", 5));

		assertThat(result.getItems()).isEmpty();
		assertThat(result.getTotalItems()).isZero();
		verify(query).variants(VariantSearchMode.ORIGINAL);
		verify(statusProvider).unpublished(query);
		verify(query).page(1, 5);
	}
}
