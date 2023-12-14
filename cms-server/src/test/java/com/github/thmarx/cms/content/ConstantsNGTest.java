package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.github.thmarx.cms.api.Constants;
import java.util.regex.Matcher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ConstantsNGTest {
	
	

	@Test
	public void test_section_pattern() {
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher("index.md").matches()).isFalse();
		Assertions.assertThat(Constants.SECTION_PATTERN.matcher(".section.md").matches()).isFalse();
		
		Matcher matcher = Constants.SECTION_PATTERN.matcher("page.section.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("section");
		Assertions.assertThat(matcher.group("section")).isEqualTo("section");
		
		matcher = Constants.SECTION_PATTERN.matcher("index.card.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group(1)).isEqualTo("card");
		Assertions.assertThat(matcher.group("section")).isEqualTo("card");
	}
	
	
	@Test
	public void test_ordered_sections_pattern() {
		Assertions.assertThat(Constants.SECTION_ORDERED_PATTERN.matcher("index.md").matches()).isFalse();
		Assertions.assertThat(Constants.SECTION_ORDERED_PATTERN.matcher(".section.md").matches()).isFalse();
		
		Matcher matcher = Constants.SECTION_ORDERED_PATTERN.matcher("page.section.md");
		Assertions.assertThat(matcher.matches()).isFalse();
		
		matcher = Constants.SECTION_ORDERED_PATTERN.matcher("page.section..md");
		Assertions.assertThat(matcher.matches()).isFalse();
		
		matcher = Constants.SECTION_ORDERED_PATTERN.matcher("index.card.1.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group("section")).isEqualTo("card");
		Assertions.assertThat(matcher.group("index")).isEqualTo("1");
		
		matcher = Constants.SECTION_ORDERED_PATTERN.matcher("index.card.10.md");
		Assertions.assertThat(matcher.matches()).isTrue();
		Assertions.assertThat(matcher.group("section")).isEqualTo("card");
		Assertions.assertThat(matcher.group("index")).isEqualTo("10");
	}
	
	@Test
	public void test_ordered_section_of() {
		
		var pattern = Constants.SECTION_ORDERED_OF_PATTERN.apply("page");
		
		var matcher = pattern.matcher("page.left.10.md");
		matcher.matches();
		Assertions.assertThat(matcher.matches()).isTrue();
	}
}
