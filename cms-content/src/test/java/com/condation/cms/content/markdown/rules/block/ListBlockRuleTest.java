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

import com.condation.cms.content.markdown.rules.block.ListBlockRule;
import com.condation.cms.content.markdown.Block;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ListBlockRuleTest {

	private ListBlockRule sut = new ListBlockRule();

	@Test
	void test_ordered_list() {

		String md = "1. Hallo\n2. Leute";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("Hallo", "Leute"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ol><li>Hallo</li><li>Leute</li></ol>");
	}

	@Test
	void test_unordered_list_star() {

		String md = "* Hallo\n* Leute";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("Hallo", "Leute"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ul><li>Hallo</li><li>Leute</li></ul>");
	}

	@Test
	void test_unordered_list_minus() {

		String md = "- Hallo\n- Leute";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("Hallo", "Leute"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ul><li>Hallo</li><li>Leute</li></ul>");
	}

	@Test
	void test_unordered_list_plus() {

		String md = "+ Hallo\n+ Leute";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("Hallo", "Leute"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ul><li>Hallo</li><li>Leute</li></ul>");
	}

	@Test
	void test_unordered_list_issue() {

		String md = "+ ul item 1\n+ ul item 2";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("ul item 1", "ul item 2"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ul><li>ul item 1</li><li>ul item 2</li></ul>");
	}
	
	@Test
	void test_dot_issue_183() {

		String md = "1. first sentence. second sentence.\n1. item 2";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("first sentence. second sentence.", "item 2"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ol><li>first sentence. second sentence.</li><li>item 2</li></ol>");
	}
	
	@Test
	void ordered_list_multiline_items() {

		String md = "1. first sentence.\nsecond sentence.\n1. item 2";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("first sentence.\nsecond sentence.", "item 2"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ol><li>first sentence.\nsecond sentence.</li><li>item 2</li></ol>");
	}
	
	@Test
	void unordered_list_multiline_items() {

		String md = "+ first sentence.\nsecond sentence.\n+ item 2";

		Block next = sut.next(md);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(ListBlockRule.ListBlock.class)
				.asInstanceOf(InstanceOfAssertFactories.type(ListBlockRule.ListBlock.class))
				.hasFieldOrPropertyWithValue("items", List.of("first sentence.\nsecond sentence.", "item 2"));

		Assertions.assertThat(next.render((content) -> content)).isEqualTo("<ul><li>first sentence.\nsecond sentence.</li><li>item 2</li></ul>");
	}
}
