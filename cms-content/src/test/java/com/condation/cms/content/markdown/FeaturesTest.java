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

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class FeaturesTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		SUT = new CMSMarkdown(Options.all());
		
	}

	@RepeatedTest(1)
	public void test_features() throws IOException {

		var md = load("features.md").trim();
		var expected = load("features.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_tables() throws IOException {

		var md = load("features.tables.md").trim();
		var expected = load("features.tables.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_definition_lists() throws IOException {

		var md = load("features.dl.md").trim();
		var expected = load("features.dl.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_tasklist() throws IOException {

		var md = load("features.tasklist.md").trim();
		var expected = load("features.tasklist.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	@RepeatedTest(1)
	public void test_tags() throws IOException {

		var md = load("features.tags.md").trim();
		var expected = load("features.tags.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	
	@Test
	void tag_with_markdown () throws IOException {
		var md = """
           [[hello]]
           **bold text**
           [[/hello]]
           """;
		var expected = """
            [[hello]]
			<strong>bold text</strong>
			[[/hello]]
            <p></p>
            """.trim();
		var result = SUT.render(md).trim();
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
}
