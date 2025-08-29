package com.condation.cms.api.db;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.condation.cms.api.Constants;
import com.condation.cms.api.db.ContentNode;
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
		Assertions.assertThat(contentNode.isVisible()).isFalse();
	}
	
	@Test
	public void test_publish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(contentNode.isVisible()).isTrue();
	}
	
	@Test
	public void test_publish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.PUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(contentNode.isVisible()).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2023() {
		var cal = Calendar.getInstance();
		cal.set(2023, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(contentNode.isVisible()).isFalse();
	}
	
	@Test
	public void test_unpublish_date_1_11_2123() {
		var cal = Calendar.getInstance();
		cal.set(2123, 11, 1);
		var contentNode = new ContentNode("", "", Map.of(
				Constants.MetaFields.UNPUBLISH_DATE, cal.getTime(),
				Constants.MetaFields.PUBLISHED, true
		));
		Assertions.assertThat(contentNode.isVisible()).isTrue();
	}
}
