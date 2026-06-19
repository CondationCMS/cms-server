package com.condation.cms.content.tags;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.content.shortcodes.ShortCodeMap;
import com.condation.cms.content.shortcodes.ShortCodeParser;
import com.condation.cms.api.request.RequestContext;
import org.apache.commons.jexl3.JexlBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author t.marx
 */
public class TagParserTest {
	
	ShortCodeParser shortCodeParser;

	ShortCodeMap shortCodeMap;
	
	RequestContext requestContext;
	
	@BeforeEach
	void setup() {
		requestContext = new RequestContext();
		
		shortCodeMap = new ShortCodeMap();
		shortCodeMap.put("code", params -> {
			// Verarbeitung der Parameter hier
			return "Ausgabe des Tags";
		});
		shortCodeMap.put("content", params -> {
			return (String)params.get("_content");
		});
		
		shortCodeMap.put("exp", params -> {
			return "expression: " + params.get("value");
		});
		
		shortCodeMap.put("param", params -> {
			return "param: " + params.get("param1");
		});
		
		shortCodeMap.put("ns1:print", params -> {
			return "message: " + params.get("message");
		});
		
		shortCodeMap.put("parent", params -> {
			return "<div class='parent'>%s</div>".formatted((String)params.get("_content"));
		});
		shortCodeMap.put("nested", params -> {
			return "nested";
		});
		
		this.shortCodeParser = new ShortCodeParser(new JexlBuilder().create());
	}

	@Test
	public void no_tag() {
		String result = shortCodeParser.parse("Dein Tag-Text hier", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Dein Tag-Text hier");
	}
	
	@Test
	public void self_closing_tag() {
		String result = shortCodeParser.parse("[[code/]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Tags");
	}
	
	@Test
	public void self_closing_tag_with_space() {
		String result = shortCodeParser.parse("[[code /]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Tags");
	}

	@Test
	public void end_closing_tag() {
		String result = shortCodeParser.parse("[[code]][[/code]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Tags");
	}
	
	@Test
	public void tag_with_content() {
		String result = shortCodeParser.parse("[[content]]Hello CondationCMS[[/content]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}
	
	@Test
	public void expressions() {
		String result = shortCodeParser.parse("[[exp value=\"${5+4}\"/]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("expression: 9");
	}
	
	@Test
	public void parameters_string() {
		shortCodeMap.put("param", params -> params.get("param1").getClass().getSimpleName());

		String result = shortCodeParser.parse("[[param param1=\"1234\"/]]", shortCodeMap, requestContext);

		Assertions.assertThat(result).isEqualTo("String");
	}
	
	@Test
	public void parameters_number() {
		shortCodeMap.put("param", params -> params.get("param1").getClass().getSimpleName());

		String result = shortCodeParser.parse("[[param param1=1234 /]]", shortCodeMap, requestContext);

		Assertions.assertThat(result).isEqualTo("Integer");
	}
	
	@Test
	public void parameters_boolean_true() {
		String result = shortCodeParser.parse("[[param param1=true /]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("param: true");
	}
	
	@Test
	public void parameters_boolean_false() {
		String result = shortCodeParser.parse("[[param param1=false /]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("param: false");
	}
	
	@Test
	public void parameters_with_content() {
		String result = shortCodeParser.parse("[[param param1=\"5\"]]Hello[[/param]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void tag_in_text() {
		String result = shortCodeParser.parse("Hello [[content]]CondationCMS[[/content]]!", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS!");
	}
	
	@Test
	public void namespace() {
		String result = shortCodeParser.parse("[[ns1:print message='Hello CondationCMS']][[/ns1:print]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("message: Hello CondationCMS");
		
		result = shortCodeParser.parse("[[ns1:print message='Hello CondationCMS' /]]", shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualTo("message: Hello CondationCMS");
	}

	@Test
	public void multiline () {
		String content = """
				[[content]]
					This is a multiline tag!
				[[/content]]
				""";

		String result = shortCodeParser.parse(content, shortCodeMap, requestContext);

		Assertions.assertThat(result).isEqualToIgnoringWhitespace("This is a multiline tag!");
	}
	
	@Test
	public void nested () {
		String content = """
                   [[parent]]
                   [[nested /]]
                   [[/parent]]
                   """;
		
		var tags = shortCodeParser.findShortCodes(content, shortCodeMap);
		Assertions.assertThat(tags.size()).isEqualTo(1);
		String result = shortCodeParser.parse(content, shortCodeMap, requestContext);
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<div class='parent'>nested</div>");
	}
}
