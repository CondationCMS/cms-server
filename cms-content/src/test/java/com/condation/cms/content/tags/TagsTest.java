package com.condation.cms.content.tags;

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


import com.condation.cms.content.tags.Tags;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.ContentBaseTest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.condation.cms.api.annotations.Tag;

/**
 *
 * @author t.marx
 */
public class TagsTest extends ContentBaseTest {
	
	static Tags tags;
	
	@BeforeEach
	public void init () {
		var builder = Tags.builder(getTagParser());
		
		builder.register(
				"youtube", 
				(params) -> "<video src='%s'></video>".formatted(params.getOrDefault("id", "")));
		builder.register(
				"hello_from", 
				(params) -> "<p><h3>%s</h3><small>from %s</small></p>".formatted(params.getOrDefault("name", ""), params.getOrDefault("from", "")));
		
		builder.register(
				"mark",
				params -> "<mark>%s</mark>".formatted(params.get("_content"))
		);
		
		builder.register(
				"mark2",
				params -> "<mark class='%s'>%s</mark>".formatted(params.get("class"), params.get("_content"))
		);
		
		builder.register(
				"exp",
				params -> "<span>%s</span>".formatted(params.get("expression"))
		);
		
		builder.register(
				"set_var",
				params -> {
					params.getRequestContext().getVariables().put("myVar", "Hello world!");
					return "";
				}
		);
		builder.register(
				"get_var",
				params -> {
					return (String)params.getRequestContext().getVariables().getOrDefault("myVar", "DEFAULT");
				}
		);
		
		builder.register(new TagHandler());
		
		tags = builder.build();
	}
	

	@Test
	void simpleTest () {
		var result = tags.replace("[[youtube    /]]");
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
		
		result = tags.replace("[[youtube/]]");
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
	}
	
	@Test
	void simple_with_text_before_and_After () {
		var result = tags.replace("before [[youtube /]] after");
		Assertions.assertThat(result).isEqualTo("before <video src=''></video> after");
	}
	
	@Test
	void complexTest () {
		
		var content = """
                some text before
                [[youtube id='id1' /]]
                some text between
				[[youtube id='id2' /]]
                some text after
                """;
		
		var result = tags.replace(content);
		
		var expected = """
                some text before
                <video src='id1'></video>
                some text between
				<video src='id2'></video>
                some text after
                """;
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	void unknown_tag () {
		var result = tags.replace("before [[vimeo id='TEST' /]] after");
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("before [[vimeo id='TEST' /]] after");
	}
	
	@Test
	void hello_from () {
		var result = tags.replace("[[hello_from name=\"Thorsten\" from=\"Bochum\" /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = tags.replace("[[hello_from name='Thorsten' from='Bochum'    /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = tags.replace("[[hello_from name='Thorsten' from='Bochum' /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
	}
	
	@Test
	void test_long () {
		var result = tags.replace("[[mark]]Important[[/mark]]");
		
		Assertions.assertThat(result).isEqualTo("<mark>Important</mark>");
	}
	
	@Test
	void test_long_with_params () {
		var result = tags.replace("[[mark2 class='test-class']]Important[[/mark2]]");
		
		Assertions.assertThat(result).isEqualTo("<mark class='test-class'>Important</mark>");
	}
	
	@Test
	void long_complex () {
		
		var content = """
                some text before
                [[mark]]Hello world![[/mark]]
                some text between
				[[mark]]Hello people![[/mark]]
                some text after
                """;
		
		var result = tags.replace(content);
		
		var expected = """
                some text before
                <mark>Hello world!</mark>
                some text between
				<mark>Hello people!</mark>
                some text after
                """;
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
	
	@Test
	void multiple_hello () {
		var input = """
              [[hello_from name='Thorsten' from='Bochum']][[/hello_from]][[hello_from name='Thorsten' from='Bochum']][[/hello_from]]
              """;
		var expected = """
              <p><h3>Thorsten</h3><small>from Bochum</small></p><p><h3>Thorsten</h3><small>from Bochum</small></p>
              """;
		var result = tags.replace(input);
		Assertions.assertThat(result).isEqualTo(expected);
		
		input = """
              [[hello_from name='Thorsten' from='Bochum'/]][[hello_from name='Thorsten' from='Bochum'/]]
              """;
		expected = """
              <p><h3>Thorsten</h3><small>from Bochum</small></p><p><h3>Thorsten</h3><small>from Bochum</small></p>
              """;
		result = tags.replace(input);
		Assertions.assertThat(result).isEqualTo(expected);
	}
	
	@Test
	void test_mismach() {
		var result = tags.replace("[[mark1 class='test-class']]Important[[/mark2]]");
		
		Assertions.assertThat(result).isEqualTo("[[mark1 class='test-class']]Important[[/mark2]]");
	}
	
	@Test
	void test_expression() {
		var result = tags.replace("[[exp expression='${meta.title}' /]]",
				Map.of(
						"meta", Map.of("title", "CondationCMS")
				)
		);
		
		Assertions.assertThat(result).isEqualTo("<span>CondationCMS</span>");
	}
	
	@Test
	void test_variables() {
		
		RequestContext requestContext = new RequestContext();
		
		tags.replace("[[set_var /]]", Map.of(), requestContext);
		
		var result = tags.replace("[[get_var /]]", Map.of(), requestContext);
		
		Assertions.assertThat(result).isEqualTo("Hello world!");
	}
	
	@Test
	void test_handler () {
		RequestContext requestContext = new RequestContext();
		
		var result = tags.replace("[[printHello name='CondationCMS' /]]", Map.of(), requestContext);
		
		Assertions.assertThat(result).isEqualTo("hello CondationCMS");
	}
	
	@Test
	void test_multiline () {
		var template = """
                 [[hello_from 
                 name=\"Thorsten\" 
                 from=\"Bochum\" /]]
                 """;
		var result = tags.replace(template);
		System.out.println(result);
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
	}
	
	public static class TagHandler {
		@Tag("printHello")
		public String printHello (Parameter parameter) {
			return "hello " + parameter.getOrDefault("name", "");
		}
	}
}
