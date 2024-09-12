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
import com.condation.cms.content.markdown.rules.block.ListBlockRule;
import java.io.IOException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class ListIssuesTest extends MarkdownTest {

	static CMSMarkdown SUT;

	@BeforeAll
	public static void setup() {
		SUT = new CMSMarkdown(Options.all());
	}

		@Test
	void test_dot_issue_183() throws IOException {

		String md = "1. **first** sentence. second sentence.\n1. item 2";
		String expected = """
                    <ol><li><strong>first</strong> sentence. second sentence.</li><li>item 2</li></ol>
                    """;

		String html = SUT.render(md);

		
		Assertions.assertThat(html).isEqualToIgnoringWhitespace(expected);
	}
}
