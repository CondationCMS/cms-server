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

import com.condation.cms.content.markdown.rules.block.TableBlockRule;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TableBlockRuleTest {

	TableBlockRule sut = new TableBlockRule();

	@Test
	public void basic_test() {

		String input = """
                 | header1 | header2 |
                 | --- | ----- |
                 | r1 / c1 | r1 / c2 |
                 | r2 / c1 | r2 / c2 |
                 """;

		String expected = """
                    <table>
						<thead>
							<tr>
                                <th>header1</th>
                                <th>header2</th>
							</tr>
                        </thead>
						<tbody>
							<tr>
                                <td>r1 / c1</td>
                                <td>r1 / c2</td>
							</tr>
							<tr>
                                <td>r2 / c1</td>
                                <td>r2 / c2</td>
							</tr>
                        </tbody>
                    </table>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(TableBlockRule.TableBlock.class);
		Assertions.assertThat(((TableBlockRule.TableBlock) next).table())
				.hasFieldOrPropertyWithValue("columns", List.of(
						new TableBlockRule.Column(Optional.empty()),
						new TableBlockRule.Column(Optional.empty())
				))
				.hasFieldOrPropertyWithValue("header", new TableBlockRule.Header(List.of(
						"header1",
						"header2"
				)))
				.hasFieldOrPropertyWithValue("rows", List.of(
						new TableBlockRule.Row(List.of("r1 / c1", "r1 / c2")),
						new TableBlockRule.Row(List.of("r2 / c1", "r2 / c2"))
				));

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void align_test() {

		String input = """
                 | header1 | header2 | header3 |
                 | :--- | :-----: | ---: |
                 | r1 / c1 | r1 / c2 | r1 / c3  |
                 | r2 / c1 | r2 / c2 | r2 / c3  |
                 """;

		String expected = """
                    <table>
						<thead>
							<tr>
                                <th style="text-align: left">header1</th>
                                <th style="text-align: center">header2</th>
                                <th style="text-align: right">header3</th>
							</tr>
                        </thead>
						<tbody>
							<tr>
                                <td style="text-align: left">r1 / c1</td>
                                <td style="text-align: center">r1 / c2</td>
                                <td style="text-align: right">r1 / c3</td>
							</tr>
							<tr>
                                <td style="text-align: left">r2 / c1</td>
                                <td style="text-align: center">r2 / c2</td>
                                <td style="text-align: right">r2 / c3</td>
							</tr>
                        </tbody>
                    </table>
                    """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(TableBlockRule.TableBlock.class);
		Assertions.assertThat(((TableBlockRule.TableBlock) next).table())
				.hasFieldOrPropertyWithValue("columns", List.of(
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.LEFT)),
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.CENTER)),
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.RIGHT))
				))
				.hasFieldOrPropertyWithValue("header", new TableBlockRule.Header(List.of(
						"header1",
						"header2",
						"header3"
				)))
				.hasFieldOrPropertyWithValue("rows", List.of(
						new TableBlockRule.Row(List.of("r1 / c1", "r1 / c2", "r1 / c3")),
						new TableBlockRule.Row(List.of("r2 / c1", "r2 / c2", "r2 / c3"))
				));

		var rendered = next.render((md) -> md);
		Assertions.assertThat(rendered).isEqualToIgnoringWhitespace(expected);
	}

	@Test
	public void align_no_header_test() {

		String input = """
                 | :--- | :-----: | ---: |
                 | r1 / c1 | r1 / c2 | r1 / c3  |
                 | r2 / c1 | r2 / c2 | r2 / c3  |
                 """;

		var next = sut.next(input);

		Assertions.assertThat(next)
				.isNotNull()
				.isInstanceOf(TableBlockRule.TableBlock.class);
		Assertions.assertThat(((TableBlockRule.TableBlock) next).table())
				.hasFieldOrPropertyWithValue("columns", List.of(
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.LEFT)),
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.CENTER)),
						new TableBlockRule.Column(Optional.of(TableBlockRule.Align.RIGHT))
				))
				.hasFieldOrPropertyWithValue("header", new TableBlockRule.Header(List.of()))
				.hasFieldOrPropertyWithValue("rows", List.of(
						new TableBlockRule.Row(List.of("r1 / c1", "r1 / c2", "r1 / c3")),
						new TableBlockRule.Row(List.of("r2 / c1", "r2 / c2", "r2 / c3"))
				));
	}

}
