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


import com.condation.cms.content.markdown.rules.block.HorizontalRuleBlockRule;
import com.condation.cms.content.markdown.Block;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author t.marx
 */
public class HorizontalRuleBlockRuleTest {
	
	private HorizontalRuleBlockRule sut = new HorizontalRuleBlockRule();

	@ParameterizedTest
	@CsvSource({
		"---",
		"***",
		"___"
	})
	void test_horizontal_rule(String input) {
		Block next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(HorizontalRuleBlockRule.HRBlock.class);

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<hr />");
	}
	
	@Test
	void test_horizontal_rule_with_before() {
		
		String input = """
                 before
                 ---
                 after
                 """;
		
		
		Block next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(HorizontalRuleBlockRule.HRBlock.class);

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<hr />");
	}
	
	@Test
	void test_horizontal_rule() {
		
		String input = """
                 
                 ---
                 
                 """;
		
		String expected = """
                    <hr />
                    """;
		
		Block next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(HorizontalRuleBlockRule.HRBlock.class);

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<hr />");
		
		Assertions.assertThat(next.render(value -> value)).isEqualToIgnoringWhitespace(expected);
	}
	
	
	@Test
	void test_horizontal_rule_issue() {
		
		String input = """
                 before
                 
                 """;
		
		
		Block next = sut.next(input);

		Assertions.assertThat(next)
				.isNull();
	}
}
