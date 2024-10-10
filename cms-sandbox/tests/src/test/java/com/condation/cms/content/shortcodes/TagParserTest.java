/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.condation.cms.content.shortcodes;

import org.apache.commons.jexl3.JexlBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author t.marx
 */
public class TagParserTest {
	
	
	TagParser tagParser;

	@BeforeEach
	void setup() {
		TagParser.Codes codes = new TagParser.Codes();
		codes.add("code", params -> {
			// Verarbeitung der Parameter hier
			return "Ausgabe des Shortcodes";
		});
		codes.add("content", params -> {
			return (String)params.get("_content");
		});
		
		codes.add("exp", params -> {
			return "expression: " + params.get("value");
		});
		
		codes.add("param", params -> {
			return "param: " + params.get("param1");
		});
		
		this.tagParser = new TagParser(codes, new JexlBuilder().create());
	}

	@Test
	public void no_shortcode() {
		String result = tagParser.parse("Dein Shortcode-Text hier");
		Assertions.assertThat(result).isEqualTo("Dein Shortcode-Text hier");
	}
	
	@Test
	public void self_closing_tag() {
		String result = tagParser.parse("[[code/]]");
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void self_closing_tag_with_space() {
		String result = tagParser.parse("[[code /]]");
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}

	@Test
	public void end_closing_tag() {
		String result = tagParser.parse("[[code]][[/code]]");
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void tag_with_content() {
		String result = tagParser.parse("[[content]]Hello CondationCMS[[/content]]");
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}
	
	@Test
	public void expressions() {
		String result = tagParser.parse("[[exp value=\"${5+4}\"/]]");
		Assertions.assertThat(result).isEqualTo("expression: 9");
	}
	
	@Test
	public void parameters_string() {
		String result = tagParser.parse("[[param param1=\"5\"/]]");
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_number() {
		String result = tagParser.parse("[[param param1=5 /]]");
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_with_content() {
		String result = tagParser.parse("[[param param1=\"5\"]]Hello[[/param]]");
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void shortCode_in_text() {
		String result = tagParser.parse("Hello [[content]]CondationCMS[[/content]]!");
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS!");
	}
	
}
