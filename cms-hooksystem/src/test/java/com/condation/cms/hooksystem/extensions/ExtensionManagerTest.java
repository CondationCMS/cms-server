package com.condation.cms.hooksystem.extensions;

/*-
 * #%L
 * CMS Extensions
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.feature.features.AuthFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.filesystem.FileSystem;
import com.condation.cms.hooksystem.CMSHookSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class ExtensionManagerTest {

	@Mock
	DB db;
	@Mock
	Theme theme;
	@Mock
	ServerProperties properties;
	@Mock
	FileSystem fileSystem;

	ExtensionManager extensionManager;

	static Engine engine;

	@BeforeAll
	public static void initEngine() {
		engine = Engine.newBuilder("js")
				.option("engine.WarnInterpreterOnly", "false")
				.build();
	}
	@AfterAll
	public static void shutdown() throws Exception {
		engine.close(true);
	}
	
	@BeforeEach
	public void setup() throws Exception {

		Mockito.when(fileSystem.resolve("libs/"))
				.thenReturn(Path.of("src/test/resources/site/libs"));
		Mockito.when(fileSystem.resolve("extensions/"))
				.thenReturn(Path.of("src/test/resources/site/extensions"));
		Mockito.when(db.getFileSystem()).thenReturn(fileSystem);
		Mockito.when(theme.extensionsPath())
				.thenReturn(Path.of("src/test/resources/theme/extensions"));

		extensionManager = new ExtensionManager(db, properties, engine);
	}

	private HookSystem setupHookSystem(RequestContext requestContext) throws IOException {
		final HookSystem hookSystem = new CMSHookSystem();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(hookSystem));
		extensionManager.newContext(theme, requestContext);
		return hookSystem;
	}

	// --- action: no arguments, reads feature context ---

	@Test
	public void test_action_no_args_with_auth() throws IOException {
		var requestContext = new RequestContext();
		requestContext.add(AuthFeature.class, new AuthFeature("thorsten"));
		var hookSystem = setupHookSystem(requestContext);

		Assertions.assertThat(hookSystem.doAction("test").results())
				.hasSize(1)
				.containsExactly("Hallo thorsten");
	}

	@Test
	public void test_action_no_args_without_auth() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("test").results())
				.hasSize(1)
				.containsExactly("Guten Tag");
	}

	// --- action: single named argument ---

	@Test
	public void test_action_single_named_arg() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("print_name", Map.of("name", "CondationCMS")).results())
				.hasSize(1)
				.containsExactly("Hallo CondationCMS");
	}

	// --- action: multiple named arguments ---

	@Test
	public void test_action_multiple_named_args() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("greet", Map.of("firstName", "Max", "lastName", "Mustermann")).results())
				.hasSize(1)
				.containsExactly("Max Mustermann");
	}

	// --- action: multiple handlers on same hook name ---

	@Test
	public void test_action_multiple_handlers_collect_all_results() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("multi/action").results())
				.hasSize(2)
				.containsExactlyInAnyOrder("result1", "result2");
	}

	// --- action: priority ordering ---

	@Test
	public void test_action_priority_ordering() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("priority/action").results())
				.hasSize(2)
				.containsExactly("low", "high");
	}

	// --- action: void return is not added to results ---

	@Test
	public void test_action_void_return_not_in_results() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("action/void").results())
				.isEmpty();
	}

	// --- filter: string transform ---

	@Test
	public void test_filter_string_to_uppercase() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doFilter("filter/upper", "hello").value())
				.isEqualTo("HELLO");
	}

	// --- filter: chained transforms in priority order ---

	@Test
	public void test_filter_chained_transforms_in_priority_order() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		// priority 100 runs first: "base-A", then priority 200: "base-A-B"
		Assertions.assertThat(hookSystem.doFilter("filter/chain", "base").value())
				.isEqualTo("base-A-B");
	}

	// --- filter: trim whitespace ---

	@Test
	public void test_filter_trim() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doFilter("filter/trim", "  hello world  ").value())
				.isEqualTo("hello world");
	}

	// --- filter: no handler registered → original value returned unchanged ---

	@Test
	public void test_filter_no_handler_returns_original_value() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doFilter("filter/unregistered", "original").value())
				.isEqualTo("original");
	}

	// --- action: no handler registered → empty results ---

	@Test
	public void test_action_no_handler_returns_empty_results() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("action/unregistered").results())
				.isEmpty();
	}
}
