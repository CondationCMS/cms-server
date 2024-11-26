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
import com.condation.cms.templates.tags.ElseIfTag;
import com.condation.cms.templates.tags.ElseTag;
import com.condation.cms.templates.tags.EndIfTag;
import com.condation.cms.templates.tags.IfTag;
import com.google.common.base.Stopwatch;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineTest {

	TemplateEngine templateEngine;

	@BeforeEach
	void setupTemplateEngine() {
		TemplateConfiguration config = new TemplateConfiguration();
		config
				.registerTag(new IfTag())
				.registerTag(new ElseIfTag())
				.registerTag(new ElseTag())
				.registerTag(new EndIfTag());
		
		config.setTemplateLoader(new StringTemplateLoader()
				.add("simple", "Hallo {{ name }}")
				.add("map", "Hallo {{ person.name }}")
				.add("text", "{{ content }}")
		);
		
		this.templateEngine = new TemplateEngine(config);
	}
	
	@RepeatedTest(5)
	public void test_simple() {
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		Template simpleTemplate = templateEngine.getTemplate("simple");
		
		System.out.println("creating simple template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
		
		Assertions.assertThat(simpleTemplate).isNotNull();
		
		Map<String, Object> context = Map.of("name", "CondationCMS");
		
		stopwatch.reset();
		stopwatch.start();
		System.out.println(simpleTemplate.execute(context));
		System.out.println("executing simple template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
	}

	@RepeatedTest(5)
	public void test_map() {
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		
		Template simpleTemplate = templateEngine.getTemplate("map");
		
		System.out.println("creating map template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
		
		Assertions.assertThat(simpleTemplate).isNotNull();
		
		Map<String, Object> context = Map.of("person", Map.of("name", "CondationCMS"));
		
		stopwatch.reset();
		stopwatch.start();
		System.out.println(simpleTemplate.execute(context));
		System.out.println("executing map template took: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
	}
	
	@Test
	public void test_escape() {
		
		Template template = templateEngine.getTemplate("text");
		
		Map<String, Object> context = Map.of("content", "<h1>heading</h1>");
		
		
		Assertions.assertThat(template.execute(context)).isEqualToIgnoringWhitespace("&lt;h1&gt;heading&lt;/h1&gt;");
	}
}
