package com.condation.cms.templates.content;

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


import com.condation.cms.content.template.functions.shortcode.ShortCodeTemplateFunction;
import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.TemplateEngineFactory;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ShortCodeTemplateFunctionTest extends ContentBaseTest {

	ShortCodes shortCodes;

	static CMSTemplateEngine engine;
	static StringTemplateLoader templateLoader;

	@BeforeAll
	public static void setup() {
		templateLoader = new StringTemplateLoader();
		engine = TemplateEngineFactory
				.newInstance(templateLoader)
				.defaultFilters()
				.defaultTags()
				.devMode(true)
				.create();
	}
	@BeforeEach
	public void setupShortCodes() {
		shortCodes = new ShortCodes(Map.of(
				"echo", (params) -> "Hello world",
				"greet", (params) -> "Hello " + params.get("name")
		), getTagParser());
	}

	@Test
	public void test_call_shortcode() throws Exception {
		String templateString = "{{shortCode.call('echo')}}";

		templateLoader.add("test_call_shortcode", templateString);
		var template = engine.getTemplate("test_call_shortcode");

		Map<String, Object> context = new HashMap<>();
		context.put("shortCode", new ShortCodeTemplateFunction(null, shortCodes));

		var content = template.evaluate(context);

		Assertions.assertThat(content).isEqualTo("Hello world");
	}

	@Test
	public void test_greet() throws Exception {
		String templateString = "{{shortCode.call('greet', {'name': 'CondationCMS'})}}";

		templateLoader.add("test_greet", templateString);
		
		var template = engine.getTemplate("test_greet");

		Map<String, Object> context = new HashMap<>();
		context.put("shortCode", new ShortCodeTemplateFunction(null, shortCodes));

		var content = template.evaluate(context);

		Assertions.assertThat(content).isEqualTo("Hello CondationCMS");
	}
}
