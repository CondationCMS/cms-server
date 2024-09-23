package com.condation.cms.content.markdown;

/*-
 * #%L
 * cms-content
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

import com.condation.cms.content.markdown.Options;
import com.condation.cms.content.markdown.CMSMarkdown;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class CMSMarkdownTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		SUT = new CMSMarkdown(Options.all());
	}

	@Test
	public void test_1() throws IOException {

		var md = load("render/test_1.md").trim();
		var expected = load("render/test_1.html");
		expected = removeComments(expected);

		var result = SUT.render(md);

		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void test_2() throws IOException {

		var md = load("render/test_2.md");
		var expected = load("render/test_2.html");
		expected = removeComments(expected);

		var result = SUT.render(md);

		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void test_3() throws IOException {

		var md = load("render/test_3.md");
		var expected = load("render/test_3.html");
		expected = removeComments(expected);

		var result = SUT.render(md);

		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void test_4() throws IOException {

		var md = load("render/test_4.md");
		var expected = load("render/test_4.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		System.out.println(result);
//		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@ParameterizedTest
	@CsvSource({
		"\\!test\\!,<p>&#33;test&#33;</p>",
		"\\|test\\|,<p>&#124;test&#124;</p>",
		"\\.test\\.,<p>&#46;test&#46;</p>",
		"\\-test\\-,<p>&#45;test&#45;</p>",
		"\\+test\\+,<p>&#43;test&#43;</p>",
		"\\(test\\),<p>&#40;test&#41;</p>",
		"\\<test\\>,<p>&#60;test&#62;</p>",
		"\\[test\\],<p>&#91;test&#93;</p>",
		"\\{test\\},<p>&#123;test&#125;</p>",
		"\\#test\\#,<p>&#35;test&#35;</p>",
		"\\_test\\_,<p>&#95;test&#95;</p>",
		"\\`test\\`,<p>&#96;test&#96;</p>",
		"\\*test\\*,<p>&#42;test&#42;</p>"
	})
	void test_escape(final String input, final String expected) throws IOException {
		var result = SUT.render(input);
		Assertions.assertThat(result).isEqualTo(expected);
	}

	@Test
	void test_horizontal_rule_with_before() throws IOException {

		String input = """
                 before
                 
                 ---
                 
                 after
                 """;

		var result = SUT.render(input);

		Assertions.assertThat(result).isEqualTo("<p>before</p><hr /><p>after</p><p></p>");
	}

	@Test
	void test_blockquote() throws IOException {

		String input = "before\n\n> line 1\n> line 2\n\nafter";

		var result = SUT.render(input);
		Assertions.assertThat(result).isEqualToIgnoringNewLines("<p>before</p><blockquote><p>line 1\nline 2</p><p></p></blockquote><p>after</p>");
	}

	@Test
	void test_blockquote_nested_heading() throws IOException {

		String input = "before\n\n> ### line 1\n>\n> line 2\n\nafter";

		var result = SUT.render(input);
		System.out.println(result);
		Assertions.assertThat(result).isEqualToIgnoringNewLines("<p>before</p><blockquote><h3 id=\"line-1\">line 1</h3><p>line 2</p><p></p></blockquote><p>after</p>");
	}

	@Test
	void test_image_link() throws IOException {

		String input = "[![An old rock in the desert](/assets/images/shiprock.jpg \"Shiprock, New Mexico by Beau Rogers\")](https://www.flickr.com)";

		var result = SUT.render(input);
		System.out.println(result);
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(
				"""
    <p>
    <a href="https://www.flickr.com" id="an-old-rock-in-the-desert">
    <img src="/assets/images/shiprock.jpg" alt="An old rock in the desert" title="Shiprock, New Mexico by Beau Rogers" />
    
      </a>
    </p>
    """
		);
	}
}
