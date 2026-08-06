package com.condation.cms.api.workflow;

/*-
 * #%L
 * CMS Core
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
import com.condation.cms.api.db.NodeVisibility;
import com.condation.cms.api.feature.features.WorkflowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import java.util.Calendar;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author thorstenmarx
 */
public class DefaultWFStatusProviderTest {

	@Test
	public void publishedQueryUsesWorkflowStatusSemantics() {
		@SuppressWarnings("unchecked")
		ContentQuery<ContentNode> query = mock(ContentQuery.class);
		String expression = "(status = 'published') OR (status NOT EXISTS AND published = true)";
		when(query.expression(expression)).thenReturn(query);

		Optional<ContentQuery<ContentNode>> result = new DefaultWFStatusProvider().published(query);

		Assertions.assertThat(result).containsSame(query);
		verify(query).expression(expression);
	}

	@Test
	public void unpublishedQueryUsesWorkflowStatusSemantics() {
		@SuppressWarnings("unchecked")
		ContentQuery<ContentNode> query = mock(ContentQuery.class);
		String expression = "(status != 'published') OR "
				+ "(status NOT EXISTS AND (published NOT EXISTS OR published = false))";
		when(query.expression(expression)).thenReturn(query);

		var result = new DefaultWFStatusProvider().unpublished(query);

		Assertions.assertThat(result).isSameAs(query);
		verify(query).expression(expression);
	}

	@Test
	public void missingPublishDateRemainsUnsetAndHasNoStartLimit() {
		var contentNode = new ContentNode("", "", "", Map.of(
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));

		var status = new DefaultWFStatusProvider().status(contentNode);

		Assertions.assertThat(status.publish_date()).isNull();
		Assertions.assertThat(status.withinSchedule()).isTrue();
	}
	
	@Test
	public void test_publish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
	
	@Test
	public void test_publish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
	
}
