
package com.condation.cms.content.markdown.rules.inline;

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


import com.condation.cms.content.markdown.rules.inline.ShortCodeInlineBlockRule;
import com.condation.cms.content.markdown.Block;
import com.condation.cms.content.markdown.InlineBlock;
import com.condation.cms.content.markdown.rules.block.ShortCodeBlockRule;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author t.marx
 */
public class ShortCodeInlineBlockRuleTest {
	
	private ShortCodeInlineBlockRule sut = new ShortCodeInlineBlockRule();

	@Test
	void long_form() {

		String md = "[[link url=\"https://google.de/\"]]Google[[/link]]";

		InlineBlock next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class))
				.hasFieldOrPropertyWithValue("tag", "link")
				.hasFieldOrPropertyWithValue("params", "url=\"https://google.de/\"")
				.hasFieldOrPropertyWithValue("content", "Google")
				;

		Assertions.assertThat(next.render()).isEqualTo("[[link url=\"https://google.de/\"]]Google[[/link]]");
	}
	
	@Test
	void short_form() {

		String md = "[[link url=\"https://google.de/\" /]]";

		InlineBlock next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class))
				.hasFieldOrPropertyWithValue("tag", "link")
				.hasFieldOrPropertyWithValue("params", "url=\"https://google.de/\"")
				;

		Assertions.assertThat(next.render()).isEqualTo("[[link url=\"https://google.de/\"]][[/link]]");
	}
	
}
