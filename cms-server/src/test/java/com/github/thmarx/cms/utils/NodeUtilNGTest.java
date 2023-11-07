package com.github.thmarx.cms.utils;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.filesystem.MetaData;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class NodeUtilNGTest {

	public NodeUtilNGTest() {
	}

	@Test
	public void getName_returns_default_name() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of());

		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("index");
	}

	@Test
	public void getName_returns_title() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title"
		));

		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("The Title");
	}

	@Test
	public void getName_returns_title_if_emtpy_menu() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title",
				"menu", Map.of(
				)
		));
		
		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("The Title");
	}
	
	@Test
	public void getName_returns_menu_title() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"title", "The Title",
				"menu", Map.of(
						"title", "Menu title"
				)
		));
		
		var name = NodeUtil.getName(node);

		Assertions.assertThat(name).isNotBlank().isEqualTo("Menu title");
	}
	
	@Test
	public void getMenuPosition() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"menu", Map.of(
						"position", 1.5
				)
		));
		var order = NodeUtil.getMenuPosition(node);
		Assertions.assertThat(order).isEqualTo(1.5f);
	}
	
	@Test
	public void getDefaultMenuPosition() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of(
				"menu", Map.of()
		));
		var position = NodeUtil.getMenuPosition(node);
		Assertions.assertThat(position).isEqualTo(Constants.DEFAULT_MENU_POSITION);
	}
	
	@Test
	public void getDefaultMenuPositionNoMenuMap() {
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index", Map.of());
		var position = NodeUtil.getMenuPosition(node);
		Assertions.assertThat(position).isEqualTo(Constants.DEFAULT_MENU_POSITION);
	}
}
