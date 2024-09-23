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

/**
 *
 * @author t.marx
 */
public class IssuesTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		SUT = new CMSMarkdown(Options.all());
	}

	@RepeatedTest(1)
	public void issue_with_multiple_code_blocks() throws IOException {

		var md = load("issues/issue.code-with-shortcode.md").trim();
		var expected = load("issues/issue.code-with-shortcode.html");
		expected = removeComments(expected);

		var result = SUT.render(md);
		result = "<div>" + result + "</div>";
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}

	
}
