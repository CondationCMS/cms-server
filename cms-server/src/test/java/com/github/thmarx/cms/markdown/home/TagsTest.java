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

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class TagsTest {

	
	@Test
	public void tag_no_parameters() {
		var matcher = TagReplacer.TAG_PARAMS_PATTERN.matcher("[[youtube]]");
		matcher.matches();
		System.out.println("tag: " + matcher.group("tag"));
	}
	
	@Test
	public void tag_with_parameters() {
		var matcher = TagReplacer.TAG_PARAMS_PATTERN.matcher("[[youtube videoid='the-id']]");
		matcher.matches();
		System.out.println("tag: " + matcher.group("tag"));
		System.out.println("params: " + matcher.group("params"));
	}
	
	@Test
	public void tag_with_multiple_parameters() {
		var matcher = TagReplacer.TAG_PARAMS_PATTERN.matcher("[[youtube videoid='the-id' param='other']]");
		matcher.matches();
		System.out.println("tag: " + matcher.group("tag"));
		System.out.println("params: " + matcher.group("params"));
	}
	
	@Test
	public void multiple_tag_with_multiple_parameters() {
		
		var content = """
                [[youtube videoid='the-id' param='other']]
                Here is some other content.
                [[youtube videoid='other-id' param='another']]
                """;
		
		var matcher = TagReplacer.TAG_PARAMS_PATTERN.matcher(content);
		while (matcher.find()) {
			System.out.println("tag: " + matcher.group("tag"));
			System.out.println("params: " + matcher.group("params"));
		}
	}
}
