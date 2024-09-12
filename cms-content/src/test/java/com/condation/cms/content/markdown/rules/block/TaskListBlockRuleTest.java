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

import com.condation.cms.content.markdown.rules.block.TaskListBlockRule;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TaskListBlockRuleTest {

	TaskListBlockRule sut = new TaskListBlockRule();

	@Test
	public void basic_test() {

		String input = """
                 - [ ] foo
                 - [x] bar
                 """;

		String expected = """
                    <ul>
						<li><input disabled="" type="checkbox" /> foo</li>
						<li><input disabled="" type="checkbox" checked="" /> bar</li>
                    </ul>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(TaskListBlockRule.TaskListBlock.class);

		Assertions.assertThat(((TaskListBlockRule.TaskListBlock) next))
				.hasFieldOrPropertyWithValue("taskList",
						new TaskListBlockRule.TaskList(List.of(
								new TaskListBlockRule.Item("foo", false),
								new TaskListBlockRule.Item("bar", true)
						))
				);

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void mulitple_test() {

		String input = """
                 
                 some paragrahp before
                 
                 - [ ] foo
                 - [x] bar
                 
                 some paragrahp after
                 """;

		String expected = """
                    <ul>
						<li><input disabled="" type="checkbox" /> foo</li>
						<li><input disabled="" type="checkbox" checked="" /> bar</li>
                    </ul>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(TaskListBlockRule.TaskListBlock.class);

		Assertions.assertThat(((TaskListBlockRule.TaskListBlock) next))
				.hasFieldOrPropertyWithValue("taskList",
						new TaskListBlockRule.TaskList(List.of(
								new TaskListBlockRule.Item("foo", false),
								new TaskListBlockRule.Item("bar", true)
						))
				);

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}

}
