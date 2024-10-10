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
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 *
 * @author t.marx
 */
public class ThymeleafShortCodeTemplateFunctionTest extends ContentBaseTest {

	ShortCodes shortCodes;

	static TemplateEngine templateEngine;

	@BeforeAll
	public static void setup() {
		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode("HTML"); // oder "TEXT" je nach Bedarf
		templateResolver.setCacheable(false);

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
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
		String templateString = "[(${shortCode.call('echo')})]";
		
		Context context = new Context();
		context.setVariable("shortCode", new ShortCodeTemplateFunction(shortCodes));
		String renderedString = templateEngine.process(templateString, context);
        Assertions.assertThat(renderedString).isEqualTo("Hello world");
	}

	@Test
	public void test_greet() throws Exception {
		String templateString = "[(${shortCode.call('greet', #{'name': 'CondationCMS'})})]";
		
		Context context = new Context();
		context.setVariable("shortCode", new ShortCodeTemplateFunction(shortCodes));
		String renderedString = templateEngine.process(templateString, context);
        Assertions.assertThat(renderedString).isEqualTo("Hello CondationCMS");
	}

}
