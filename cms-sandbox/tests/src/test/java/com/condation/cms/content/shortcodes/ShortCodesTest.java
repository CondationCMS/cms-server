package com.condation.cms.content.shortcodes;

/*-
 * #%L
 * tests
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author t.marx
 */
public class ShortCodesTest {

	ShortCodes shortCodes;
	ShortCodes.Codes codes = new ShortCodes.Codes();

	@BeforeEach
	void setup() {
		shortCodes = new ShortCodes();
		
		codes.add("code", params -> {
			// Verarbeitung der Parameter hier
			return "Ausgabe des Shortcodes";
		});
		codes.add("content", params -> {
			return (String)params.get("content");
		});
		
		codes.add("exp", params -> {
			return "expression: " + params.get("value");
		});
		
		codes.add("param", params -> {
			return "param: " + params.get("param1");
		});
	}

	@Test
	public void no_shortcode() {
		String result = shortCodes.parseShortcodes("Dein Shortcode-Text hier", codes);
		Assertions.assertThat(result).isEqualTo("Dein Shortcode-Text hier");
	}
	
	@Test
	public void self_closing_tag() {
		String result = shortCodes.parseShortcodes("[[code/]]", codes);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void self_closing_tag_with_space() {
		String result = shortCodes.parseShortcodes("[[code /]]", codes);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}

	@Test
	public void end_closing_tag() {
		String result = shortCodes.parseShortcodes("[[code]][[/code]]", codes);
		Assertions.assertThat(result).isEqualTo("Ausgabe des Shortcodes");
	}
	
	@Test
	public void tag_with_content() {
		String result = shortCodes.parseShortcodes("[[content]]Hello CondationCMS[[/content]]", codes);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}
	
	@Test
	public void expressions() {
		String result = shortCodes.parseShortcodes("[[exp value=\"${5+4}\"/]]", codes);
		Assertions.assertThat(result).isEqualTo("expression: 9");
	}
	
	@Test
	public void parameters_string() {
		String result = shortCodes.parseShortcodes("[[param param1=\"5\"/]]", codes);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_number() {
		String result = shortCodes.parseShortcodes("[[param param1=5 /]]", codes);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void parameters_with_content() {
		String result = shortCodes.parseShortcodes("[[param param1=\"5\"]]Hello[[/param]]", codes);
		Assertions.assertThat(result).isEqualTo("param: 5");
	}
	
	@Test
	public void shortCode_in_text() {
		String result = shortCodes.parseShortcodes("Hello [[content]]CondationCMS[[/content]]!", codes);
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS!");
	}
}
