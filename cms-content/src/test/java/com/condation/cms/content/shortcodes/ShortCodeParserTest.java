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


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ShortCodeParserTest {

	@Test
	public void testParseShortcodes_singleShortcodeWithContent() {
		String text = "This is a text with a shortcode [[code1 param1=\"value1\" param2=\"value2\"]]This is content[[/code1]].";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(1, shortcodes.size());

		var shortcode = shortcodes.get(0);
		assertEquals("code1", shortcode.getName());
		assertEquals("value1", shortcode.getParameters().get("param1"));
		assertEquals("value2", shortcode.getParameters().get("param2"));
		assertEquals("This is content", shortcode.getContent());
	}

	@Test
	public void testParseShortcodes_multipleShortcodes() {
		String text = "This is a text with a shortcode [[code1 param1=\"value1\" param2=\"value2\"]]This is content[[/code1]] and another one [[code2 param1=\"value1\" param3=\"value3\" /]].";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(2, shortcodes.size());

		var shortcode1 = shortcodes.get(0);
		assertEquals("code1", shortcode1.getName());
		assertEquals("value1", shortcode1.getParameters().get("param1"));
		assertEquals("value2", shortcode1.getParameters().get("param2"));
		assertEquals("This is content", shortcode1.getContent());

		var shortcode2 = shortcodes.get(1);
		assertEquals("code2", shortcode2.getName());
		assertEquals("value1", shortcode2.getParameters().get("param1"));
		assertEquals("value3", shortcode2.getParameters().get("param3"));
		assertEquals("", shortcode2.getContent());
	}
	
	@Test
	public void testParseShortcodes_multipleShortcodes2() {
		String text = "This is a text with a shortcode [[code1 param1=\"value1\" param2=\"value2\" ]]This is content[[/code1]] and another one [[code2 param1=\"value1\" param3=\"value3\"/]].";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(2, shortcodes.size());

		var shortcode1 = shortcodes.get(0);
		assertEquals("code1", shortcode1.getName());
		assertEquals("value1", shortcode1.getParameters().get("param1"));
		assertEquals("value2", shortcode1.getParameters().get("param2"));
		assertEquals("This is content", shortcode1.getContent());

		var shortcode2 = shortcodes.get(1);
		assertEquals("code2", shortcode2.getName());
		assertEquals("value1", shortcode2.getParameters().get("param1"));
		assertEquals("value3", shortcode2.getParameters().get("param3"));
		assertEquals("", shortcode2.getContent());
	}

	@Test
	public void testParseShortcodes_noShortcodes() {
		String text = "This text has no shortcodes.";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(0, shortcodes.size());
	}

	@Test
	public void testParseShortcodes_emptyParameters() {
		String text = "This is a text with a shortcode [[code1]][[/code1]] and another one [[code2 /]].";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(2, shortcodes.size());

		var shortcode1 = shortcodes.get(0);
		assertEquals("code1", shortcode1.getName());
		assertEquals("", shortcode1.getContent());

		var shortcode2 = shortcodes.get(1);
		assertEquals("code2", shortcode2.getName());
		assertEquals("", shortcode2.getContent());
	}

	@Test
	public void testParseShortcodes_malformedShortcodes() {
		String text = "This is a text with a malformed shortcode [[code1 param1=\"value1\" param2=\"value2\" .";
		List<ShortCodeParser.Match> shortcodes = ShortCodeParser.parseShortcodes(text);

		assertEquals(0, shortcodes.size());
	}
}
