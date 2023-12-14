package com.github.thmarx.cms.markdown.home;

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
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TagReplaceTest {

	
	TagReplacer tagReplacer = new TagReplacer();
	{
		tagReplacer.add("youtube", (params) -> "<video src='%s'></video>".formatted(params.get("id")));
		tagReplacer.add("hello_from", (params) -> "<p><h3>%s</h3><small>from %s</small></p>".formatted(params.get("name"), params.get("from")));
	}
	
	@Test
	void simpleTest () {
		var result = tagReplacer.replace("[[youtube]]");
		Assertions.assertThat(result).isEqualTo("<video src='null'></video>");
	}
	
	@Test
	void simple_with_text_before_and_After () {
		var result = tagReplacer.replace("before [[youtube]] after");
		Assertions.assertThat(result).isEqualTo("before <video src='null'></video> after");
	}
	
	@Test
	void complexTest () {
		
		var content = """
                some text before
                [[youtube id='id1']]
                some text between
				[[youtube id='id2']]
                some text after
                """;
		
		var result = tagReplacer.replace(content);
		
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
		var result = tagReplacer.replace("before [[vimeo id='TEST']] after");
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("before [[vimeo id='TEST']] after");
	}
	
	@Test
	void hello_from () {
		var result = tagReplacer.replace("[[hello_from name='Thorsten',from='Bochum']]");
		Assertions.assertThat(result).isEqualTo("<p><h3>Thorsten</h3><small>from Bochum</small></p>");
	}
}
