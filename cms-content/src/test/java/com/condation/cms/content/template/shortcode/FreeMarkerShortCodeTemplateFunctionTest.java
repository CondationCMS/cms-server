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


import com.condation.cms.content.ContentBaseTest;
import com.condation.cms.content.template.functions.shortcode.ShortCodeTemplateFunction;
import com.condation.cms.content.shortcodes.ShortCodes;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
public class FreeMarkerShortCodeTemplateFunctionTest extends ContentBaseTest {

	static Configuration cfg;
	
	ShortCodes shortCodes;
	
	@BeforeAll
	public static void setup() {
		cfg = new Configuration(Configuration.VERSION_2_3_33);
		cfg.setDefaultEncoding("UTF-8");
	}

	@BeforeEach
	public void setupShortCodes() {
		shortCodes = new ShortCodes(Map.of(
				"echo", (params) -> "Hello world",
				"greet", (params) -> "Hello " + params.get("name")
		), getTagParser());
	}
	
	@Test
	public void testSomeMethod() throws Exception {
		String templateString = "${shortCode.call('echo')}";
		Template template = new Template("templateName", new StringReader(templateString), cfg);

		Map<String, Object> model = new HashMap<>();
		model.put("shortCode", new ShortCodeTemplateFunction(null, shortCodes));
		Writer out = new StringWriter();
		template.process(model, out);

		String renderedString = out.toString();
		Assertions.assertThat(renderedString).isEqualTo("Hello world");
	}
	
	@Test
	public void test_greet() throws Exception {
		String templateString = "${shortCode.call('greet', {'name':'CondationCMS'})}";
		Template template = new Template("templateName", new StringReader(templateString), cfg);

		Map<String, Object> model = new HashMap<>();
		model.put("shortCode", new ShortCodeTemplateFunction(null, shortCodes));
		Writer out = new StringWriter();
		template.process(model, out);

		String renderedString = out.toString();
		Assertions.assertThat(renderedString).isEqualTo("Hello CondationCMS");
	}

}
