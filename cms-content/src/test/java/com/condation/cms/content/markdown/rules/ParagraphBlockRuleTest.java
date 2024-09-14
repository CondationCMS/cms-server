package com.condation.cms.content.markdown.rules;

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


import com.condation.cms.content.markdown.rules.block.ParagraphBlockRule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ParagraphBlockRuleTest {
	
	ParagraphBlockRule paragraphRule = new ParagraphBlockRule();

	@Test
	public void testSomeMethod() {
		String content = """
                   Hallo
                   Leute
                   """;
		
		var block = paragraphRule.next(content.trim());
		
		Assertions.assertThat(block).isNotNull();
		
		Assertions.assertThat(((ParagraphBlockRule.ParagraphBlock)block).content()).isEqualTo("Hallo\nLeute");
	}
	
	@Test
	public void test_multiple() {
		String content = """
                   Hallo\n\nLeute
                   """;
		
		var block = paragraphRule.next(content.trim());
		
		Assertions.assertThat(block).isNotNull();
		
		Assertions.assertThat(((ParagraphBlockRule.ParagraphBlock)block).content()).isEqualTo("Hallo");
	}
	
}