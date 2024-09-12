package com.condation.cms.extensions;

/*-
 * #%L
 * cms-extensions
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


import com.condation.cms.extensions.GlobalExtensions;
import com.condation.cms.api.hooks.HookSystem;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author t.marx
 */
public class GlobalExtensionsTest {
	
	static Context context;
	static HookSystem hookSystem;
	
	static GlobalExtensions globalExtensions;
	
	@BeforeAll
	public static void initEngine() throws IOException {
		context = Context.newBuilder()
				.allowAllAccess(true)
				.allowHostClassLookup(className -> true)
				.allowHostAccess(HostAccess.ALL)
				.allowValueSharing(true)
				.build();
		
		hookSystem = new HookSystem();
		globalExtensions = new GlobalExtensions(hookSystem, context);
		globalExtensions.init();

	}
	@AfterAll
	public static void shutdown() throws Exception {
		context.close(true);
	}

	@Test
	public void test_init() {
		globalExtensions.evaluate("console.log($hooks != null)");
	}
	
	@Test
	public void test_hook () {
		globalExtensions.evaluate("$hooks.registerAction('test/hook1', (context) => {return 'Hello';})");
		
		var context = hookSystem.execute("test/hook1");
		
		Assertions.assertThat(context.results())
				.hasSize(1)
				.containsExactly("Hello");
	}
	
}
