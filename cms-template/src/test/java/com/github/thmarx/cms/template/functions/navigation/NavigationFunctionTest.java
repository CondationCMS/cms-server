package com.github.thmarx.cms.template.functions.navigation;

/*-
 * #%L
 * cms-template
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.thmarx.cms.api.content.ContentParser;
import com.github.thmarx.cms.api.db.Content;
import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.db.DBFileSystem;
import com.github.thmarx.cms.api.feature.features.ContentNodeMapperFeature;
import com.github.thmarx.cms.api.feature.features.ContentParserFeature;
import com.github.thmarx.cms.api.feature.features.HookSystemFeature;
import com.github.thmarx.cms.api.feature.features.MarkdownRendererFeature;
import com.github.thmarx.cms.api.hooks.FilterContext;
import com.github.thmarx.cms.api.hooks.HookSystem;
import com.github.thmarx.cms.api.mapper.ContentNodeMapper;
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.model.NavNode;
import com.github.thmarx.cms.api.request.RequestContext;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class NavigationFunctionTest {

	RequestContext requestContext;
	HookSystem hookSystem;
	
	@Mock
	DB db;
	@Mock
	DBFileSystem fileSystem;
	@Mock
	Content content;
	@Mock
	ContentParser contentParser;
	@Mock
	MarkdownRenderer markdownRenderer;
	@Mock
	ContentNodeMapper contentNodeMapper;
	
	NavigationFunction sut;
	
	@BeforeEach
	public void setup () {
		
		var contentBase = Path.of("content/");
		
		Mockito.lenient().when(db.getFileSystem()).thenReturn(fileSystem);
		Mockito.lenient().when(db.getContent()).thenReturn(content);
		Mockito.lenient().when(fileSystem.resolve("content/")).thenReturn(contentBase);
		Mockito.lenient().when(content.byUri("current")).thenReturn(Optional.empty());
		
		
		hookSystem = new HookSystem();
		requestContext = new RequestContext();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));
		requestContext.add(ContentParserFeature.class, new ContentParserFeature(contentParser));
		requestContext.add(MarkdownRendererFeature.class, new MarkdownRendererFeature(markdownRenderer));
		requestContext.add(ContentNodeMapperFeature.class, new ContentNodeMapperFeature(contentNodeMapper));
		
		sut = new NavigationFunction(db, Path.of("content/current/"), requestContext);
	}
	
	@Test
	public void test_hook_path() {
		var hookCalled = new AtomicBoolean(false);
		hookSystem.registerFilter("navigation/test/path", (FilterContext<NavNode> parameters) -> {
			hookCalled.set(true);
			
			var nodes = (List<NavNode>) parameters.values();
			
			nodes.add(0, new NavNode("test", "test", false));
			
			return nodes;
		});
		
		var nodes = sut.named("test").path();
		
		Assertions.assertThat(hookCalled).isTrue();
		Assertions.assertThat(nodes).containsExactly(
				new NavNode("test", "test", false)
		);
	}
	
	@Test
	public void test_hook_list() {
	}
}
