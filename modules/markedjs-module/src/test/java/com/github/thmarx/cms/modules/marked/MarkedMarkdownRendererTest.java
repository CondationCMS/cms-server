package com.github.thmarx.cms.modules.marked;

/*-
 * #%L
 * markedjs-module
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
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.feature.features.IsPreviewFeature;
import com.github.thmarx.cms.api.feature.features.SitePropertiesFeature;
import com.github.thmarx.cms.api.request.RequestContext;
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MarkedMarkdownRendererTest {

	private static Engine engine;
	private static MarkedMarkdownRenderer sut;

	@BeforeAll
	public static void setup() {
		engine = Engine.create();
		sut = new MarkedMarkdownRenderer(Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.engine(engine).build());
	}

	@AfterAll
	public static void clean() {
		sut.close();
		engine.close();
	}

	@Test
	public void test_simple_markdown() {
		var result = sut.render("**Bold**");

		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><strong>Bold</strong></p>");
	}

	@Test
	public void test_link() {
		var result = sut.render("[Link text Here](https://link-url-here.org)");

		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"https://link-url-here.org\">Link text Here</a></p>");
	}

	@Test
	public void test_link_preview() {
		RequestContext context = new RequestContext();
		context.add(IsPreviewFeature.class, new IsPreviewFeature());
		ThreadLocalRequestContext.REQUEST_CONTEXT.set(context);
		try {
			var result = sut.render("[Link text Here](/internal/url)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"/internal/url?preview\">Link text Here</a></p>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}

	@Test
	public void test_link_preview_append() {
		RequestContext context = new RequestContext();
		context.add(IsPreviewFeature.class, new IsPreviewFeature());
		ThreadLocalRequestContext.REQUEST_CONTEXT.set(context);
		try {
			var result = sut.render("[Link text Here](/internal/url?hello=world)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"/internal/url?hello=world&preview\">Link text Here</a></p>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}

	@Test
	public void test_link_preview_extern() {
		RequestContext context = new RequestContext();
		context.add(IsPreviewFeature.class, new IsPreviewFeature());
		ThreadLocalRequestContext.REQUEST_CONTEXT.set(context);
		try {
			var result = sut.render("[Link text Here](http://external.org/url?hello=world)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"http://external.org/url?hello=world\">Link text Here</a></p>");

			result = sut.render("[Link text Here](https://external.org/url?hello=world)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"https://external.org/url?hello=world\">Link text Here</a></p>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}

	@Test
	public void test_heading_id() {
		var result = sut.render("# heading");
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<h1 id=\"heading\">heading</h1>");

	}
	
	@Test
	public void test_link_with_context() {
		RequestContext context = new RequestContext();
		context.add(SitePropertiesFeature.class, 		
				new SitePropertiesFeature(new SiteProperties(Map.of("context_path", "/de"))));
		ThreadLocalRequestContext.REQUEST_CONTEXT.set(context);
		try {
			var result = sut.render("[Link text Here](/internal/url)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"/de/internal/url\">Link text Here</a></p>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}
	
	@Test
	public void test_link_with_context_in_preview() {
		RequestContext context = new RequestContext();
		context.add(IsPreviewFeature.class, new IsPreviewFeature());
		context.add(SitePropertiesFeature.class, 		
				new SitePropertiesFeature(new SiteProperties(Map.of("context_path", "/de"))));
		ThreadLocalRequestContext.REQUEST_CONTEXT.set(context);
		try {
			var result = sut.render("[Link text Here](/internal/url)");
			Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><a href=\"/de/internal/url?preview\">Link text Here</a></p>");
		} finally {
			ThreadLocalRequestContext.REQUEST_CONTEXT.remove();
		}
	}
}
