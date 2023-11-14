package com.github.thmarx.cms.modules.marked;

/*-
 * #%L
 * markedjs-module
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

import org.assertj.core.api.Assertions;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MarkedMarkdownRendererTest {
	
	private static Engine engine;
	private static MarkedMarkdownRenderer sut;

	@BeforeAll
	public static void setup () {
		engine = Engine.create();
		sut = new MarkedMarkdownRenderer(Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.engine(engine).build());
	}
	
	@AfterAll
	public static void clean () {
		sut.close();
		engine.close();
	}
	
	@Test
	public void test_simple_markdown() {
		var result = sut.render("**Bold**");
		
		Assertions.assertThat(result).isEqualToIgnoringWhitespace("<p><strong>Bold</strong></p>");
	}
	
}
