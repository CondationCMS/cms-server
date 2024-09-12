package com.condation.cms.content.markdown.rules.block;

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


import com.condation.cms.content.markdown.rules.block.HeadingBlockRule;
import com.condation.cms.content.markdown.Block;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class HeadingBlockRuleTest {

	private HeadingBlockRule sut = new HeadingBlockRule();

	@ParameterizedTest
	@CsvSource({
		"# Wonderful heading level 1,Wonderful heading level 1,1",
		"## Wonderful heading level 2,Wonderful heading level 2,2",
		"### Wonderful heading level 3,Wonderful heading level 3,3",
		"#### Wonderful heading level 4,Wonderful heading level 4,4",
		"##### Wonderful heading level 5,Wonderful heading level 5,5",
		"###### Wonderful heading level 6,Wonderful heading level 6,6",
	})
	void test_h_levels(String md, String heading, int level) {
		

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(HeadingBlockRule.HeadingBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(HeadingBlockRule.HeadingBlock.class))
				.hasFieldOrPropertyWithValue("level", level)
				.hasFieldOrPropertyWithValue("heading", heading);
	}

	@Test
	void test_h_blocks() {
		
		String md = "# Heading\nnext paragraph";
		String heading = "Heading";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(HeadingBlockRule.HeadingBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(HeadingBlockRule.HeadingBlock.class))
				.hasFieldOrPropertyWithValue("heading", heading);
	}
}
