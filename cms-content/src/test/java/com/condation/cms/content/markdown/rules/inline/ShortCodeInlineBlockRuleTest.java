
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


import com.condation.cms.content.markdown.InlineBlock;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

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
				.isInstanceOf(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class);
		
		var tag = (ShortCodeInlineBlockRule.ShortCodeInlineBlock)next;
		Assertions.assertThat(tag.tagInfo())
				.hasFieldOrPropertyWithValue("name", "link")
				.hasFieldOrPropertyWithValue("rawAttributes", Map.of(
						"url", "https://google.de/",
						"_content", "Google"
				));

		Assertions.assertThat(next.render()).isEqualTo("[[link url=\"https://google.de/\"]]Google[[/link]]");
	}
	
	@Test
	void short_form() {

		String md = "[[link url=\"https://google.de/\" /]]";

		InlineBlock next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeInlineBlockRule.ShortCodeInlineBlock.class);

		var tag = (ShortCodeInlineBlockRule.ShortCodeInlineBlock)next;
		Assertions.assertThat(tag.tagInfo())
				.hasFieldOrPropertyWithValue("name", "link")
				.hasFieldOrPropertyWithValue("rawAttributes", Map.of(
						"url", "https://google.de/"
				));
		
		Assertions.assertThat(next.render()).isEqualTo("[[link url=\"https://google.de/\"]][[/link]]");
	}
	
}
