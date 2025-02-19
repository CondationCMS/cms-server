package com.condation.cms.templates;

/*-
 * #%L
 * templates
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

import com.condation.cms.templates.utils.TemplateUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TemplateUtilsTest {

	@Test
	public void no_filter() {
		Assertions.assertThat(TemplateUtils.hasFilters("var")).isFalse();

		Assertions.assertThat(TemplateUtils.extractFilters("var")).isEmpty();

		Assertions.assertThat(TemplateUtils.extractVariableName("var")).isEqualTo("var");
	}

	@Test
	public void one_filter() {
		Assertions.assertThat(TemplateUtils.hasFilters("var | trim")).isTrue();

		Assertions.assertThat(TemplateUtils.extractFilters("var | trim")).containsExactly("trim");

		Assertions.assertThat(TemplateUtils.extractVariableName("var")).isEqualTo("var");
	}

	@Test
	public void more_filter() {
		Assertions.assertThat(TemplateUtils.hasFilters("var | trim | upper | raw")).isTrue();

		Assertions.assertThat(TemplateUtils.extractFilters("var | trim | upper | raw")).containsExactly("trim", "upper",
				"raw");

		Assertions.assertThat(TemplateUtils.extractVariableName("var")).isEqualTo("var");
	}

	@Test
	public void filter_with_params() {
		Assertions.assertThat(TemplateUtils.hasFilters("var | trim(100) | raw(html)")).isTrue();

		Assertions.assertThat(TemplateUtils.extractFilters("var | trim(100) | raw(html)")).containsExactly("trim(100)",
				"raw(html)");

		Assertions.assertThat(TemplateUtils.extractVariableName("var")).isEqualTo("var");
	}

	@Test
	public void filter_parse_with_params() {
		var filter = TemplateUtils.parseFilter("truncate(20, 'ellipsis')");

		Assertions.assertThat(filter.name()).isEqualTo("truncate");
		Assertions.assertThat(filter.parameters())
				.hasSize(2)
				.containsExactly("20", "'ellipsis'");
	}
	
	@Test
	public void filter_parse_without_params() {
		var filter = TemplateUtils.parseFilter("raw");

		Assertions.assertThat(filter.name()).isEqualTo("raw");
		Assertions.assertThat(filter.parameters())
				.isEmpty();;
	}
	
	@Test
	public void complex_filter() {
		Assertions.assertThat(TemplateUtils.hasFilters("node.meta['date'] | date")).isTrue();
	}
}
