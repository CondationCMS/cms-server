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
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineMacroTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("simple", """
                   {% macro hello(name) %}
						Hello {{ name }}!
                   {% endmacro %}
                   {{ hello('CondationCMS') }}
                   """)
				.add("param", """
                   {% macro hello(name) %}
						Hello {{ name }}!
                   {% endmacro %}
                   {{ hello(name) }}
                   """)
				.add("import", """
                   {% import "simple" %}
                   {{ hello(name) }}
                   """)
				.add("namespace", """
                   {% import "simple" as fn %}
                   {{ fn.hello(name) }}
                   """)
				.add("namespace_expression", """
					{% assign template = "simple" %}
					{% import template as fn %}
					{{ fn.hello(name) }}
                   """);

	}

	@Test
	public void test_simple() throws IOException {
		Template simpleTemplate = SUT.getTemplate("simple");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate()).isEqualToIgnoringWhitespace("Hello CondationCMS!");
	}

	@Test
	public void test_param() throws IOException {
		Template simpleTemplate = SUT.getTemplate("param");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate(Map.of("name", "Developer"))).isEqualToIgnoringWhitespace("Hello Developer!");
	}

	@Test
	public void test_import() throws IOException {
		Template simpleTemplate = SUT.getTemplate("import");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate(Map.of("name", "CondationCMS"))).isEqualToIgnoringWhitespace("Hello CondationCMS!");
	}

	@Test
	public void test_namespace() throws IOException {
		Template simpleTemplate = SUT.getTemplate("namespace");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate(Map.of("name", "CondationCMS"))).isEqualToIgnoringWhitespace("Hello CondationCMS!");
	}
	
	@Test
	public void test_namespace_expression() throws IOException {
		Template simpleTemplate = SUT.getTemplate("namespace_expression");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate(Map.of("name", "CondationCMS"))).isEqualToIgnoringWhitespace("Hello CondationCMS!");
	}
}
