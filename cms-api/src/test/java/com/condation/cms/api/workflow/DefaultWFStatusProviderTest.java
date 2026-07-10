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
import com.condation.cms.api.db.NodeVisibility;
import com.condation.cms.api.feature.features.WorkFlowFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import java.util.Calendar;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class DefaultWFStatusProviderTest {
	
	@Test
	public void test_publish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
	
	@Test
	public void test_publish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.STATUS, DefaultWFStatusProvider.STATUS_PUBLISHED
		));
		Assertions.assertThat(NodeVisibility.isVisible(contentNode)).isTrue();
	}
	
}
