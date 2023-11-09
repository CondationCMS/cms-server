package com.github.thmarx.cms.content;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.regex.Matcher;
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
		
		contentTags = new ContentTags(tags);
	}
	

	@Test
	void simpleTest () {
		var result = contentTags.replace("[[youtube    /]]");
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
