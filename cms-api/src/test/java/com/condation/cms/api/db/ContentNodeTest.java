package com.condation.cms.api.db;

/*-
 * #%L
 * CMS Api
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
import com.condation.cms.api.feature.features.WFStatusProviderFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import java.util.Calendar;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ContentNodeTest {
	

	@Test
	public void test_publish() {
		var contentNode = new ContentNode("", "", Map.of());
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
		Assertions.assertThat(contentNode.isVisible()).isFalse();
	}

	@Test
	public void test_custom_wf_status_provider() {
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISHED, false
		));
		var requestContext = new RequestContext();
		requestContext.add(WFStatusProviderFeature.class, new WFStatusProviderFeature(new WFStatusProvider() {
			@Override
			public boolean isPublished(ContentNode node) {
				return true;
			}

			@Override
			public Status status(ContentNode node) {
				return new Status(true, true);
			}
		}));

		ScopedValue.where(RequestContextScope.REQUEST_CONTEXT, requestContext).run(() -> {
			Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
			Assertions.assertThat(contentNode.isVisible()).isTrue();
		});
	}
	
	@Test
	public void test_publish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
	
	@Test
	public void test_publish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
}
