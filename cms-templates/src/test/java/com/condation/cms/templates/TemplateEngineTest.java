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
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("simple", "Hallo {{ name }}")
				.add("map", "Hallo {{ person.name }}")
				.add("text", "{{ content }}")
				.add("text_raw", "{{ content | raw }}")
				.add("text_expression_raw", "{{ ns.content | raw }}");
		
	}
	
	@RepeatedTest(5)
	public void test_simple() throws IOException {
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		Template simpleTemplate = SUT.getTemplate("simple");
		
		System.out.println("creating simple template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
		
		Assertions.assertThat(simpleTemplate).isNotNull();
		
		Map<String, Object> context = Map.of("name", "CondationCMS");
		
		stopwatch.reset();
		stopwatch.start();
		System.out.println(simpleTemplate.evaluate(context));
		System.out.println("executing simple template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
	}

	@RepeatedTest(5)
	public void test_map() throws IOException {
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		Template simpleTemplate = SUT.getTemplate("map");
		
		System.out.println("creating map template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
		
		Assertions.assertThat(simpleTemplate).isNotNull();
		
		Map<String, Object> context = Map.of("person", Map.of("name", "CondationCMS"));
		
		stopwatch.reset();
		stopwatch.start();
		System.out.println(simpleTemplate.evaluate(context));
		System.out.println("executing map template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
	}
	
	@Test
	public void test_escape() throws IOException {
		
		Template template = SUT.getTemplate("text");
		
		Map<String, Object> context = Map.of("content", "<h1>heading</h1>");
		
		
		Assertions.assertThat(template.evaluate(context)).isEqualToIgnoringWhitespace("&lt;h1&gt;heading&lt;/h1&gt;");
	}
	
	@Test
	public void test_raw() throws IOException {
		
		Template template = SUT.getTemplate("text_raw");
		
		Map<String, Object> context = Map.of("content", "<h1>heading</h1>");
		
		
		Assertions.assertThat(template.evaluate(context)).isEqualToIgnoringWhitespace("<h1>heading</h1>");
	}
	
	@Test
	public void test_expression_raw() throws IOException {
		
		Template template = SUT.getTemplate("text_expression_raw");
		
		Map<String, Object> context = Map.of("ns", Map.of("content", "<h1>heading</h1>"));
		
		
		Assertions.assertThat(template.evaluate(context)).isEqualToIgnoringWhitespace("<h1>heading</h1>");
	}
}
