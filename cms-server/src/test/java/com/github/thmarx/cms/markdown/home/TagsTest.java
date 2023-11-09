package com.github.thmarx.cms.markdown.home;

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
