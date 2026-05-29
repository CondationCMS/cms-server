package com.condation.cms.content.pipeline;

/*-
 * #%L
 * CMS Content
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
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.hooksystem.CMSHookSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HTMLPipelineTest {

	private HookSystem hookSystem;
	private HTMLPipeline pipeline;

	@BeforeEach
	void setUp() {
		hookSystem = new CMSHookSystem();
		pipeline = new HTMLPipeline(hookSystem);
	}

	@Test
	void shouldInjectHeader() {
		// given
		String html = """
            <html>
            <head>
            </head>
            <body>
            content
            </body>
            </html>
        """;

		hookSystem.registerAction(Hooks.LAYOUT_HEADER.hook(),
				(context) -> "<script src='a.js'></script>");
		// when
		String result = pipeline.process(html);

		// then
		assertThat(result)
				.contains("<script src='a.js'></script>")
				.contains("</head>");
	}

	@Test
	void shouldInjectFooter() {
		// given
		String html = """
            <html>
            <head>
            </head>
            <body>
            content
            </body>
            </html>
        """;

		hookSystem.registerAction(Hooks.LAYOUT_FOOTER.hook(),
				(context) -> "<script>footer()</script>");

		// when
		String result = pipeline.process(html);

		// then
		assertThat(result)
				.contains("<script>footer()</script>")
				.contains("</body>");
	}

	@Test
	void shouldInjectBoth() {
		// given
		String html = """
            <html>
            <head>
            </head>
            <body>
            content
            </body>
            </html>
        """;

		hookSystem.registerAction(Hooks.LAYOUT_HEADER.hook(),
				(context) -> "<script>head()</script>");
		hookSystem.registerAction(Hooks.LAYOUT_FOOTER.hook(),
				(context) -> "<script>footer()</script>");

		// when
		String result = pipeline.process(html);

		// then
		assertThat(result)
				.contains("<script>head()</script>")
				.contains("<script>footer()</script>")
				.contains("</head>")
				.contains("</body>");
	}

	@Test
	void shouldIgnoreEmptyHooks() {
		// given
		String html = """
            <html>
            <head>
            </head>
            <body>
            content
            </body>
            </html>
        """;

		// when
		String result = pipeline.process(html);

		// then
		assertThat(result)
				.doesNotContain("<script")
				.contains("</head>")
				.contains("</body>");
	}

	@Test
	void shouldNotFailIfNoHeadOrBodyExists() {
		// given
		String html = "<div>no layout tags</div>";

		hookSystem.registerAction(Hooks.LAYOUT_HEADER.hook(),
				(context) -> "<script>head()</script>");
		hookSystem.registerAction(Hooks.LAYOUT_FOOTER.hook(),
				(context) -> "<script>footer()</script>");

		// when
		String result = pipeline.process(html);

		// then
		assertThat(result)
				.isEqualTo(html);
	}
}
