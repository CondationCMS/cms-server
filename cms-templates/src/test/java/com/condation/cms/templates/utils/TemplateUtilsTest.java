package com.condation.cms.templates.utils;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thorstenmarx
 */
public class TemplateUtilsTest {
	
	public TemplateUtilsTest() {
	}

	@Test
	public void test_filter_issue() {
		var filters = TemplateUtils.extractFilters("test | date('MMM d, yyyy')");
		Assertions.assertThat(filters).hasSize(1);
		Assertions.assertThat(filters.getFirst()).isEqualTo("date('MMM d, yyyy')");
		
		var filter = TemplateUtils.parseFilter(filters.getFirst());
		Assertions.assertThat(filter.name()).isEqualTo("date");
		Assertions.assertThat(filter.parameters())
				.hasSize(1)
				.contains("'MMM d, yyyy'");
	}
	
}
