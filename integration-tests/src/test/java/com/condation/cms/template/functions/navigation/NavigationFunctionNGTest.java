package com.condation.cms.template.functions.navigation;

/*-
 * #%L
 * cms-server
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



import com.condation.cms.TestHelper;
import com.condation.cms.api.Constants;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.db.cms.NIOReadOnlyFile;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.mapper.ContentNodeMapper;
import com.condation.cms.api.markdown.MarkdownRenderer;
import com.condation.cms.api.model.NavNode;
import com.condation.cms.content.DefaultContentParser;
import com.condation.cms.content.template.functions.navigation.NavigationFunction;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.filesystem.FileDB;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class NavigationFunctionNGTest {

	static NavigationFunction navigationFunction;
	private static FileDB db;
	static MarkdownRenderer markdownRenderer = TestHelper.getRenderer();
	static Path hostBase = Path.of("hosts/test/");

	@AfterAll
	static void close () throws Exception {
		db.close();
	}
	@BeforeAll
	static void init() throws IOException {
		var contentParser = new DefaultContentParser();
		var config = new Configuration();
		db = new FileDB(Path.of("hosts/test"), new DefaultEventBus(), (file) -> {
			try {
				ReadOnlyFile cmsFile = new NIOReadOnlyFile(file, hostBase.resolve(Constants.Folders.CONTENT));
				return contentParser.parseMeta(cmsFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, config);
		db.init();
		defaultContentParser = new DefaultContentParser();
		navigationFunction = new NavigationFunction(db, 
				db.getReadOnlyFileSystem().contentBase().resolve("nav/index.md"),
				TestHelper.requestContext("/", defaultContentParser, markdownRenderer, new ContentNodeMapper(db, defaultContentParser)));
	}
	protected static DefaultContentParser defaultContentParser;

	@Test
	public void test_root() {

		List<NavNode> list = navigationFunction.list("/nav");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/nav", "/nav/folder1");
	}

	@Test
	public void test_folder1() {

		List<NavNode> list = navigationFunction.list("/nav/folder1");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/nav/folder1/test", "/nav/folder1");
	}

	@Test
	public void test_draft() {

		List<NavNode> list = navigationFunction.list("/nav2");

		Assertions.assertThat(list).isEmpty();
	}
	
	@Test
	public void test_visibility() {

		List<NavNode> list = navigationFunction.list("/visibility");

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/visibility/folder1");
	}
	
	@Test
	public void test_path() throws Exception {

		var sut = new NavigationFunction(db, 
				db.getReadOnlyFileSystem().contentBase().resolve("nav3/folder1/index.md")
				, 
				TestHelper.requestContext("/", defaultContentParser, markdownRenderer, new ContentNodeMapper(db, defaultContentParser)));
		
		List<NavNode> path = sut.path();

		Assertions.assertThat(path).hasSize(3);
		Assertions.assertThat(path.get(0).path()).isEqualTo("/");
		Assertions.assertThat(path.get(1).path()).isEqualTo("/nav3");
		Assertions.assertThat(path.get(2).path()).isEqualTo("/nav3/folder1");
	}
	
	@Test
	public void test_json () throws IOException {
		var navigationFunction = new NavigationFunction(db, 
				db.getReadOnlyFileSystem().contentBase().resolve("nav/index.md"),
				TestHelper.requestContext("/", defaultContentParser, markdownRenderer, new ContentNodeMapper(db, defaultContentParser)));
		
		List<NavNode> list = navigationFunction.json().list("/json");
		Assertions.assertThat(list).hasSize(1);
		Assertions.assertThat(list.get(0).name()).isEqualTo("JSON");
		
		list = navigationFunction.html().list("/json");
		Assertions.assertThat(list).hasSize(1);
		Assertions.assertThat(list.get(0).name()).isEqualTo("HTML");
	}
	
	@Test
	public void test_subnav() {

		List<NavNode> list = navigationFunction.list("/subnav", 2);

		var nodeUris = list.stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/subnav", "/subnav/folder1");
		
		NavNode navNode = list.stream().filter(node -> node.path().equals("/subnav/folder1")).findFirst().get();
		
		Assertions.assertThat(navNode.children()).hasSize(1);
		nodeUris = navNode.children().stream().map(NavNode::path).collect(Collectors.toList());
		Assertions.assertThat(nodeUris)
				.containsExactlyInAnyOrder("/subnav/folder1/folder2");
		
	}
	
}
