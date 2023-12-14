package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
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
public class ContentTagsTest {
	
	static ContentTags contentTags;
	
	@BeforeAll
	public static void init () {
		ContentTags.Tags tags = new ContentTags.Tags();
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
		
		contentTags = new ContentTags(tags);
	}
	

	@Test
	void simpleTest () {
		var result = contentTags.replace("[[youtube    /]]");
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
		
		result = contentTags.replace("[[youtube/]]");
		Assertions.assertThat(result).isEqualTo("<video src=''></video>");
	}
	
	@Test
	void simple_with_text_before_and_After () {
		var result = contentTags.replace("before [[youtube /]] after");
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
		
		var result = contentTags.replace(content);
		
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
		var result = contentTags.replace("before [[vimeo id='TEST' /]] after");
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("before  after");
	}
	
	@Test
	void hello_from () {
		var result = contentTags.replace("[[hello_from name='Thorsten',from='Bochum' /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = contentTags.replace("[[hello_from name='Thorsten',from='Bochum'    /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
		
		result = contentTags.replace("[[hello_from name='Thorsten', from='Bochum' /]]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
	}
	
	@Test
	void test_long () {
		var result = contentTags.replace("[[mark]]Important[[/mark]]");
		
		Assertions.assertThat(result).isEqualTo("<mark>Important</mark>");
	}
	
	@Test
	void test_long_with_params () {
		var result = contentTags.replace("[[mark2 class='test-class']]Important[[/mark2]]");
		
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
		
		var result = contentTags.replace(content);
		
		var expected = """
                some text before
                <mark>Hello world!</mark>
                some text between
				<mark>Hello people!</mark>
                some text after
                """;
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace(expected);
	}
}
