package com.github.thmarx.cms.template.functions.navigation;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.content.ContentParser;
import com.github.thmarx.cms.TestHelper;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.eventbus.DefaultEventBus;
import com.github.thmarx.cms.filesystem.FileSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class NavigationFunctionNGTest {

	static NavigationFunction navigationFunction;
	private static FileSystem fileSystem;
	static MarkdownRenderer markdownRenderer = TestHelper.getRenderer();

	@BeforeAll
	static void init() throws IOException {
		var contentParser = new ContentParser();
		fileSystem = new FileSystem(Path.of("hosts/test"), new DefaultEventBus(), (file) -> {
			try {
				return contentParser.parseMeta(file);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		fileSystem.init();
		navigationFunction = new NavigationFunction(fileSystem, Path.of("hosts/test/content/nav/index.md"), new ContentParser(),
				markdownRenderer);
	}

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
	public void test_path() {

		var sut = new NavigationFunction(fileSystem, Path.of("hosts/test/content/nav3/folder1/index.md"), new ContentParser(),
				markdownRenderer);
		
		List<NavNode> path = sut.path();

		Assertions.assertThat(path).hasSize(3);
		Assertions.assertThat(path.get(0).path()).isEqualTo("/");
		Assertions.assertThat(path.get(1).path()).isEqualTo("/nav3");
		Assertions.assertThat(path.get(2).path()).isEqualTo("/nav3/folder1");
	}
}
