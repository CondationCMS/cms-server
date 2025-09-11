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
import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineFilterTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("var_default", "{{ content | default('the default text') }}")
				.add("simple", "{{ meta['date'] | date }}")
				.add("month_year", "{{ meta['date'] | date('MM/yyyy')}}")
				.add("format_issue", "{{ meta['date'] | date('MMM d, yyyy')}}")
				;

	}

	@Test
	public void test_simple() throws IOException {

		Template simpleTemplate = SUT.getTemplate("simple");
		Assertions.assertThat(simpleTemplate).isNotNull();

		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		var date = new Date();

		Map<String, Object> context = Map.of("meta", Map.of(
				"date", date
		));

		var result = simpleTemplate.evaluate(context);

		Assertions.assertThat(result).isEqualTo(format.format(date));
	}

	@Test
	public void test_month_year() throws IOException {

		Template simpleTemplate = SUT.getTemplate("month_year");
		Assertions.assertThat(simpleTemplate).isNotNull();

		SimpleDateFormat format = new SimpleDateFormat("MM/yyyy");
		var date = new Date();

		Map<String, Object> context = Map.of("meta", Map.of(
				"date", date
		));

		var result = simpleTemplate.evaluate(context);

		Assertions.assertThat(result).isEqualTo(format.format(date));
	}
	
	@Test
	public void test_default_filter() throws IOException {

		Template simpleTemplate = SUT.getTemplate("var_default");
		Assertions.assertThat(simpleTemplate).isNotNull();

		var result = simpleTemplate.evaluate(
				Map.of("content", "&lt;p&gt;&lt;/p&gt;")
		);

		Assertions.assertThat(result).isEqualTo("the default text");		
		
		result = simpleTemplate.evaluate(
				Map.of("content", "<p></p>")
		);

		Assertions.assertThat(result).isEqualTo("the default text");
		
		result = simpleTemplate.evaluate(
				Map.of("content", "")
		);

		Assertions.assertThat(result).isEqualTo("the default text");
	}
	
	@Test
	public void issue_format() throws IOException {

		Template simpleTemplate = SUT.getTemplate("format_issue");
		Assertions.assertThat(simpleTemplate).isNotNull();

		SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy");
		var date = new Date();

		Map<String, Object> context = Map.of("meta", Map.of(
				"date", date
		));

		var result = simpleTemplate.evaluate(context);

		Assertions.assertThat(result).isEqualTo(format.format(date));
	}
}
