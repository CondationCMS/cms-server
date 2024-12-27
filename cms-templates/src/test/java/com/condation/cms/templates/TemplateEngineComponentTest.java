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
import com.condation.cms.content.shortcodes.ShortCodes;
import com.condation.cms.content.shortcodes.TagParser;
import com.condation.cms.templates.exceptions.ParserException;
import com.condation.cms.templates.exceptions.RenderException;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thmar
 */
public class TemplateEngineComponentTest extends AbstractTemplateEngineTest {

	static ShortCodes shortCodes;
	static DynamicConfiguration dynamicConfiguration;

	@BeforeAll
	public void setupShortCodes() {
		shortCodes = new ShortCodes(
				Map.of(
						"tag1", (params) -> {
							return "Hello";
						},
						"tag2", (param) -> {
							return "Hello " + param.get("name") + "!";
						},
						"tag3", (param) -> {
							return "<div>%s</div>".formatted(param.get("_content"));
						}),
				new TagParser(null)
		);
		dynamicConfiguration = new DynamicConfiguration(shortCodes);
	}

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("tag1", """
                   {[ tag1 ]}
						
                   {[ endtag1 ]}
                   """)
				.add("tag2", """
                   {[ tag2 name="CondationCMS" ]}
                   {[ endtag2 ]}
                   """)
				.add("tag3", """
                   {[ tag3 ]}
						This is the content!
                   {[ endtag3 ]}
                   """)
				.add("parser_exception", """
                          {[ tag3 ]}
                          	This is the content!
                          {[ endtag4 ]}
                          """)
				.add("render_exception", """
                          {[ tag4 ]}
                          	This is the content!
                          {[ endtag4 ]}
                          """);
	}

	@Test
	public void test_tag1() throws IOException {
		Template simpleTemplate = SUT.getTemplate("tag1");
		Assertions.assertThat(simpleTemplate).isNotNull();

		Assertions
				.assertThat(simpleTemplate.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("Hello");
	}

	@Test
	public void test_tag2() throws IOException {
		Template simpleTemplate = SUT.getTemplate("tag2");
		Assertions.assertThat(simpleTemplate).isNotNull();

		Assertions
				.assertThat(simpleTemplate.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("Hello CondationCMS!");
	}

	@Test
	public void test_tag3() throws IOException {
		Template simpleTemplate = SUT.getTemplate("tag3");
		Assertions.assertThat(simpleTemplate).isNotNull();

		Assertions
				.assertThat(simpleTemplate.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("<div>This is the content!</div>");
	}

	@Test
	public void test_parser_exception() throws IOException {
		Assertions.assertThatThrownBy(() -> SUT.getTemplate("parser_exception"))
				.isInstanceOf(ParserException.class);
	}
	
	@Test
	public void test_render_exception() throws IOException {
		var template = SUT.getTemplate("render_exception");
		Assertions.assertThatThrownBy(() -> template.evaluate(Map.of(), dynamicConfiguration))
				.isInstanceOf(RenderException.class);
	}
}
