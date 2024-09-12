package com.condation.cms.content.template.shortcode;

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
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class PebbleShortCodeTemplateFunctionTest {

	static ShortCodes shortCodes;

	static PebbleEngine engine;

	@BeforeAll
	public static void setup() {
		engine = new PebbleEngine.Builder()
				.loader(new StringLoader())
				.build();

		shortCodes = new ShortCodes(Map.of(
				"echo", (params) -> "Hello world",
				"greet", (params) -> "Hello " + params.get("name")
		));
	}

	@Test
	public void testSomeMethod() throws Exception {
		String templateString = "{{shortCode.call('echo')}}";

		PebbleTemplate template = engine.getTemplate(templateString);

		Map<String, Object> context = new HashMap<>();
		context.put("shortCode", new ShortCodeTemplateFunction(shortCodes));

		Writer writer = new StringWriter();
		template.evaluate(writer, context);

		Assertions.assertThat(writer.toString()).isEqualTo("Hello world");
	}

	@Test
	public void test_greet() throws Exception {
		String templateString = "{{shortCode.call('greet', {'name': 'CondationCMS'})}}";

		PebbleTemplate template = engine.getTemplate(templateString);

		Map<String, Object> context = new HashMap<>();
		context.put("shortCode", new ShortCodeTemplateFunction(shortCodes));

		Writer writer = new StringWriter();
		template.evaluate(writer, context);

		Assertions.assertThat(writer.toString()).isEqualTo("Hello CondationCMS");
	}
}
