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

import com.condation.cms.content.markdown.rules.block.DefinitionListBlockRule;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class DefinitionListBlockRuleTest {

	DefinitionListBlockRule sut = new DefinitionListBlockRule();

	@Test
	public void basic_test() {

		String input = """
                 a list about things
                 : thing one
                 : thing two
                 """;

		String expected = """
                    <dl>
						<dt>a list about things</dt>
						<dd>thing one</dd>
						<dd>thing two</dd>
                    </dl>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(DefinitionListBlockRule.DefinitionListBlock.class);
		Assertions.assertThat(((DefinitionListBlockRule.DefinitionListBlock) next).listContainer())
				.hasFieldOrPropertyWithValue("lists", List.of(
						new DefinitionListBlockRule.DefinitionList("a list about things", List.of(
								"thing one",
								"thing two"
						))
				));

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	public void mulitple_test() {

		String input = """
                 a list about things
                 : thing one
                 : thing two
                 a second list
                 : second
                 : other second
                 """;

		String expected = """
                    <dl>
						<dt>a list about things</dt>
						<dd>thing one</dd>
						<dd>thing two</dd>
						<dt>a second list</dt>
                    	<dd>second</dd>
                    	<dd>other second</dd>
                    </dl>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(DefinitionListBlockRule.DefinitionListBlock.class);
		Assertions.assertThat(((DefinitionListBlockRule.DefinitionListBlock) next).listContainer())
				.hasFieldOrPropertyWithValue("lists", List.of(
						new DefinitionListBlockRule.DefinitionList("a list about things", List.of(
								"thing one",
								"thing two"
						)),
						new DefinitionListBlockRule.DefinitionList("a second list", List.of(
								"second",
								"other second"
						))
				));

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}

}
