package com.condation.cms.content.shortcodes;

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


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class ShortCodeParserReplaceTest {
	
	static ShortCodeParser.Codes tags;
	
	@BeforeAll
	public static void init () {
		
		tags = new ShortCodeParser.Codes();
		
		tags.add(
				"youtube", 
				(params) -> "<video src='%s'></video>".formatted(params.getOrDefault("id", "")));
		tags.add(
				"hello_from", 
				(params) -> "<p><h3>%s</h3><small>from %s</small></p>".formatted(params.getOrDefault("name", ""), params.getOrDefault("from", "")));
		
		tags.add(
				"mark",
				params -> "<mark>%s</mark>".formatted(params.get("content"))
		);
		
		tags.add(
				"mark2",
				params -> "<mark class='%s'>%s</mark>".formatted(params.get("class"), params.get("content"))
		);
	}
	

	@Test
	void simpleTest () {
		var result = ShortCodeParser.replace("[[youtube    /]]", tags);
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
		
		result = ShortCodeParser.replace("[[youtube/]]", tags);
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
	}
	
	@Test
	void simple_with_text_before_and_After () {
		var result = ShortCodeParser.replace("before [[youtube /]] after", tags);
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
		
		var result = ShortCodeParser.replace(content, tags);
		
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
		var result = ShortCodeParser.replace("before [[vimeo id='TEST' /]] after", tags);
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("before  after");
	}
	
	@Test
	void hello_from () {
		var result = ShortCodeParser.replace("[[hello_from name='Thorsten' from='Bochum' /]]", tags);
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = ShortCodeParser.replace("[[hello_from name='Thorsten' from='Bochum'    /]]", tags);
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = ShortCodeParser.replace("[[hello_from name='Thorsten' from='Bochum' /]]", tags);
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
	}
	
	@Test
	void test_long () {
		var result = ShortCodeParser.replace("[[mark]]Important[[/mark]]", tags);
		
		Assertions.assertThat(result).isEqualTo("<mark>Important</mark>");
	}
	
	@Test
	void test_long_with_params () {
		var result = ShortCodeParser.replace("[[mark2 class='test-class']]Important[[/mark2]]", tags);
		
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
		
		var result = ShortCodeParser.replace(content,tags);
		
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
		var result = ShortCodeParser.replace(input, tags);
		Assertions.assertThat(result).isEqualTo(expected);
		
		input = """
              [[hello_from name='Thorsten' from='Bochum'/]][[hello_from name='Thorsten' from='Bochum'/]]
              """;
		expected = """
              <p><h3>Thorsten</h3><small>from Bochum</small></p><p><h3>Thorsten</h3><small>from Bochum</small></p>
              """;
		result = ShortCodeParser.replace(input, tags);
		Assertions.assertThat(result).isEqualTo(expected);
	}
}
