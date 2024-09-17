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


import com.condation.cms.content.markdown.rules.block.BlockquoteBlockRule;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class BlockquoteBlockRuleTest {

	BlockquoteBlockRule sut = new BlockquoteBlockRule();

	@Test
	public void testBlockRule() {
		
		String input = "\n> block 1\n> block 2";
		
		var next = sut.next(input);
		
		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(BlockquoteBlockRule.BlockquoteBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(BlockquoteBlockRule.BlockquoteBlock.class))
				.hasFieldOrPropertyWithValue("content", "block 1\nblock 2");
	}

}
