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


import com.condation.cms.content.ContentBaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ShortCodeParserExpressionsTest extends ContentBaseTest {

	@Test
	public void test_numbers() {
		String text = "[[code param1=\"12\" param2=\"12.5\"]][[/code]].";
		List<ShortCodeParser.Match> shortcodes = getShortCodeParser().parseShortcodes(text);

		assertEquals(1, shortcodes.size());

		var shortcode = shortcodes.get(0);
		assertEquals(12, shortcode.getParameters().get("param1"));
		assertEquals(12.5, shortcode.getParameters().get("param2"));
	}
	
	@Test
	public void test_list() {
		String text = "[[code param1=\"[12, 13, 14]\" param2=\"['12', '13', '14]\"]][[/code]].";
		List<ShortCodeParser.Match> shortcodes = getShortCodeParser().parseShortcodes(text);

		var shortcode = shortcodes.get(0);
		assertEquals(List.of(12, 13, 14), shortcode.getParameters().get("param1"));
		assertEquals(List.of("12", "13", "14"), shortcode.getParameters().get("param2"));
	}
}
