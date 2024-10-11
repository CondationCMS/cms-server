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


import com.condation.cms.content.markdown.Block;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ShortCodeBlockRuleTest {
	
	private ShortCodeBlockRule sut = new ShortCodeBlockRule();

	@Test
	void long_form() {

		String md = "[[link url=\"https://google.de/\"]]Google[[/link]]";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeBlockRule.ShortCodeBlock.class);

		var tag = (ShortCodeBlockRule.ShortCodeBlock)next;
		Assertions.assertThat(tag.tagInfo())
				.hasFieldOrPropertyWithValue("name", "link")
				.hasFieldOrPropertyWithValue("rawAttributes", Map.of(
						"url", "https://google.de/",
						"_content", "Google"
				));
		
		Assertions.assertThat(next.render((content) -> content)).isEqualTo("[[link url=\"https://google.de/\"]]Google[[/link]]");
	}
	
	@Test
	void short_form() {

		String md = "[[link url=\"https://google.de/\" /]]";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeBlockRule.ShortCodeBlock.class);

		var tag = (ShortCodeBlockRule.ShortCodeBlock)next;
		Assertions.assertThat(tag.tagInfo())
				.hasFieldOrPropertyWithValue("name", "link")
				.hasFieldOrPropertyWithValue("rawAttributes", Map.of(
						"url", "https://google.de/"
				));
		
		Assertions.assertThat(next.render((content) -> content)).isEqualTo("[[link url=\"https://google.de/\"]][[/link]]");
	}
	
	@Test
	void test_issue () {
		String md = "[[video type=\"youtube\" id=\"y0sF5xhGreA\" title=\"Everybody loves little cats\" /]]";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ShortCodeBlockRule.ShortCodeBlock.class)
				;

		var tag = (ShortCodeBlockRule.ShortCodeBlock)next;
		Assertions.assertThat(tag.tagInfo())
				.hasFieldOrPropertyWithValue("name", "video")
				.hasFieldOrPropertyWithValue("rawAttributes", Map.of(
						"type", "youtube",
						"id", "y0sF5xhGreA",
						"title", "Everybody loves little cats"
				));
		
		Assertions.assertThat(next.render((content) -> content))
				.isEqualTo("[[video id=\"y0sF5xhGreA\" title=\"Everybody loves little cats\" type=\"youtube\"]][[/video]]");

	}
}
