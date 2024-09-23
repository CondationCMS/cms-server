package com.condation.cms.content.markdown.rules.block;

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


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class CodeBlockRuleTest {

	private CodeBlockRule CodeBlockRule = new CodeBlockRule();

	@Test
	public void testSomeMethod() {
		
		String code = "```java\ndas ist java code\n```\n\n```php\ndas ist php code\n```";
		
		var codeBlock = (CodeBlockRule.CodeBlock)CodeBlockRule.next(code.trim());
		Assertions.assertThat(codeBlock.language()).isEqualTo("java");
		Assertions.assertThat(codeBlock.content()).isEqualTo("das ist java code");
	}
	
	@Test
	public void test_html() {
		
		String code = "```html\n<h2>heading</h2>\n```";
		
		var codeBlock = (CodeBlockRule.CodeBlock)CodeBlockRule.next(code.trim());
		Assertions.assertThat(codeBlock.language()).isEqualTo("html");
		Assertions.assertThat(codeBlock.content()).isEqualTo("<h2>heading</h2>");
	}
}
