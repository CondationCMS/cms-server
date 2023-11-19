package com.github.thmarx.cms.markdown;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class MarkdTest {

	static Engine engine;
	static Context context;
	static Source markedSource;
	static Source purifySource;

	static Value markedFunction;
	
	@BeforeAll
	public static void init() throws IOException {
		engine = Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
		context = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.engine(engine).build();
		
		var content = new String(MarkdTest.class.getResourceAsStream("marked.min.js").readAllBytes(), StandardCharsets.UTF_8);
		markedSource = Source.newBuilder("js", content, "marked.mjs").build();
		context.eval(markedSource);
		markedFunction = context.eval("js", "(function (param) {return marked.parse(param);})");
	}
	@AfterAll
	public static void close () {
		context.close();
		engine.close();
	}
	
	@Test
	public void renderMarkDown () {
		try (var cxt = Context.newBuilder("js")
				.engine(engine)
				.allowHostAccess(HostAccess.ALL)
				.build()) {
			cxt.eval(markedSource);
			
			cxt.getBindings("js").putMember("markdown", "# markd rulezz!");
			Value value = cxt.eval("js", "marked.parse(markdown)");
			System.out.println(value.asString());
		}
	}
	
	@Test
	public void global () {
		System.out.println(markedFunction.execute("*bold*"));
	}

}
