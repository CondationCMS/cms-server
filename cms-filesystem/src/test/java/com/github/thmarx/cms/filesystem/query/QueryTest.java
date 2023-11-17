package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.filesystem.MetaData;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class QueryTest {
	
	private static Collection<MetaData.MetaNode> nodes;

	@BeforeAll
	public static void setup (){
		nodes = new ArrayList<>();
		MetaData.MetaNode node = new MetaData.MetaNode("/", "index.md", Map.of(
				"featured", true, 
				"published", Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));
		nodes.add(node);
		node = new MetaData.MetaNode("/2", "index2.md", Map.of(
				"featured", true, 
				"published", Date.from(Instant.now().minus(1, ChronoUnit.DAYS))));
		nodes.add(node);
		node = new MetaData.MetaNode("/test1", "test1.md", Map.of(
				"featured", false, 
				"index", 1, "published", Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
				"tags", List.of("three", "four")
		));
		nodes.add(node);
		node = new MetaData.MetaNode("/test2", "test2.md", Map.of(
				"featured", false, 
				"index", 2, 
				"published",	Date.from(Instant.now().minus(1, ChronoUnit.DAYS)),
				"tags", List.of("one", "two"))
		);
		nodes.add(node);
	}
	
	@Test
	public void test_is() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").is(true).get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.getFirst().uri()).isEqualTo("/2");
	}
	
	@Test
	public void test_not() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").not(true).get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.stream().map(MetaData.MetaNode::uri).toList()).contains("/test1", "/test2");
	}
	
	@Test
	public void test_data() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.get();
		Assertions.assertThat(nodes).hasSize(3);
	}
	
	@Test
	public void test_sort_asc() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").is(false).sort("index").asc().get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
		Assertions.assertThat(nodes.get(1).uri()).isEqualTo("/test2");
	}
	
	@Test
	public void test_sort_desc() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").is(false).sort("index").desc().get();
		Assertions.assertThat(nodes).hasSize(2);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
		Assertions.assertThat(nodes.get(1).uri()).isEqualTo("/test1");
	}
	
	@Test
	public void test_offset_0() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").is(false).sort("index").desc().get(0, 1);
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}
	
	@Test
	public void test_offset_1() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("featured").is(false).sort("index").desc().get(1, 1);
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}
	
	@Test
	public void test_contains() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("tags").contains("one").get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test2");
	}
	
	@Test
	public void test_contains_not() {
		Query<MetaData.MetaNode> query = new Query<>(nodes, (node) -> node);
		var nodes = query.where("tags").contains_not("one").get();
		Assertions.assertThat(nodes).hasSize(1);
		Assertions.assertThat(nodes.get(0).uri()).isEqualTo("/test1");
	}
}
