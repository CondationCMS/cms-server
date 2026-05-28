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
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import com.condation.cms.extensions.ExtensionManager;
import com.condation.cms.filesystem.FileSystem;
import com.condation.cms.hooksystem.CMSHookSystem;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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

	@Test
	public void test_action_single_named_args() throws IOException {
		var hookSystem = setupHookSystem(new RequestContext());

		Assertions.assertThat(hookSystem.doAction("print_name_args", Map.of("name", "CondationCMS")).results())
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

	// --- template functions registered via $hooks ---

	private TemplateFunctionWrapper setupTemplateFunctions(RequestContext requestContext) throws IOException {
		setupHookSystem(requestContext);
		return new TemplateHooks(requestContext).getTemplateFunctions();
	}

	@Test
	public void test_template_function_default_namespace_registered() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		Assertions.assertThat(wrapper.getRegisterTemplateFunctions())
				.anyMatch(f -> f.namespace().equals("ext") && f.name().equals("hello"));
	}

	@Test
	public void test_template_function_no_params_returns_correct_value() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		var fn = wrapper.getRegisterTemplateFunctions().stream()
				.filter(f -> f.name().equals("hello")).findFirst().orElseThrow();
		Assertions.assertThat(fn.function().apply(new Parameter())).isEqualTo("Hello World");
	}

	@Test
	public void test_template_function_destructured_param() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		var fn = wrapper.getRegisterTemplateFunctions().stream()
				.filter(f -> f.name().equals("greet")).findFirst().orElseThrow();
		Assertions.assertThat(fn.function().apply(new Parameter(Map.of("name", "CondationCMS"))))
				.isEqualTo("Hello CondationCMS");
	}

	@Test
	public void test_template_function_destructured_param_js_default() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		var fn = wrapper.getRegisterTemplateFunctions().stream()
				.filter(f -> f.name().equals("greet")).findFirst().orElseThrow();
		Assertions.assertThat(fn.function().apply(new Parameter())).isEqualTo("Hello stranger");
	}

	@Test
	public void test_template_function_multiple_params() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		var fn = wrapper.getRegisterTemplateFunctions().stream()
				.filter(f -> f.name().equals("full_name")).findFirst().orElseThrow();
		Assertions.assertThat(fn.function().apply(new Parameter(Map.of("firstName", "Max", "lastName", "Mustermann"))))
				.isEqualTo("Max Mustermann");
	}

	@Test
	public void test_template_function_explicit_namespace_registered() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		Assertions.assertThat(wrapper.getRegisterTemplateFunctions())
				.anyMatch(f -> f.namespace().equals("theme") && f.name().equals("version"));
	}

	@Test
	public void test_template_function_explicit_namespace_returns_correct_value() throws IOException {
		var wrapper = setupTemplateFunctions(new RequestContext());

		var fn = wrapper.getRegisterTemplateFunctions().stream()
				.filter(f -> f.name().equals("version")).findFirst().orElseThrow();
		Assertions.assertThat(fn.function().apply(new Parameter())).isEqualTo("1.0.0");
	}

	// --- template components registered via $hooks ---

	private TemplateComponentsWrapper setupComponents(RequestContext requestContext) throws IOException {
		setupHookSystem(requestContext);
		return new TemplateHooks(requestContext).getComponents(new HashMap<>());
	}

	@Test
	public void test_template_component_default_namespace_registered() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		Assertions.assertThat(wrapper.getComponents()).containsKey("ext:badge");
	}

	@Test
	public void test_template_component_no_params_returns_correct_value() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		String result = wrapper.getComponents().get("ext:badge").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("<span class='badge'>badge</span>");
	}

	@Test
	public void test_template_component_destructured_param() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		String result = wrapper.getComponents().get("ext:alert")
				.apply(new Parameter(Map.of("message", "Watch out!")));
		Assertions.assertThat(result).isEqualTo("<div class='alert'>Watch out!</div>");
	}

	@Test
	public void test_template_component_destructured_param_js_default() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		String result = wrapper.getComponents().get("ext:alert").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("<div class='alert'>default</div>");
	}

	@Test
	public void test_template_component_explicit_namespace_registered() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		Assertions.assertThat(wrapper.getComponents()).containsKey("theme:card");
	}

	@Test
	public void test_template_component_explicit_namespace_returns_correct_value() throws IOException {
		var wrapper = setupComponents(new RequestContext());

		String result = wrapper.getComponents().get("theme:card")
				.apply(new Parameter(Map.of("title", "My Card")));
		Assertions.assertThat(result).isEqualTo("<div class='card'>My Card</div>");
	}

	// --- tags registered via $tags.register ---

	private TagsWrapper setupTags(RequestContext requestContext) throws IOException {
		var hookSystem = setupHookSystem(requestContext);
		var codes = new HashMap<String, java.util.function.Function<Parameter, String>>();
		return new ContentHooks(requestContext).getTags(codes);
	}

	@Test
	public void test_tag_default_namespace_registered() throws IOException {
		var wrapper = setupTags(new RequestContext());

		Assertions.assertThat(wrapper.getTags()).containsKey("ext:hello");
	}

	@Test
	public void test_tag_default_namespace_no_params_returns_correct_value() throws IOException {
		var wrapper = setupTags(new RequestContext());

		String result = wrapper.getTags().get("ext:hello").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("Hello World");
	}

	@Test
	public void test_tag_default_namespace_with_named_param() throws IOException {
		var wrapper = setupTags(new RequestContext());

		String result = wrapper.getTags().get("ext:greet").apply(new Parameter(Map.of("name", "CondationCMS")));
		Assertions.assertThat(result).isEqualTo("Hello CondationCMS");
	}

	@Test
	public void test_tag_default_namespace_with_missing_param_uses_default() throws IOException {
		var wrapper = setupTags(new RequestContext());

		String result = wrapper.getTags().get("ext:greet").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("Hello stranger");
	}

	@Test
	public void test_tag_explicit_namespace_registered() throws IOException {
		var wrapper = setupTags(new RequestContext());

		Assertions.assertThat(wrapper.getTags()).containsKey("theme:info");
	}

	@Test
	public void test_tag_explicit_namespace_returns_correct_value() throws IOException {
		var wrapper = setupTags(new RequestContext());

		String result = wrapper.getTags().get("theme:info").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("theme-info");
	}

	@Test
	public void test_tag_multiple_destructured_params() throws IOException {
		var wrapper = setupTags(new RequestContext());

		String result = wrapper.getTags().get("ext:full_name")
				.apply(new Parameter(Map.of("firstName", "Max", "lastName", "Mustermann")));
		Assertions.assertThat(result).isEqualTo("Max Mustermann");
	}

	@Test
	public void test_tag_destructured_param_with_js_default() throws IOException {
		var wrapper = setupTags(new RequestContext());

		// no "name" attribute → JS default value kicks in
		String result = wrapper.getTags().get("ext:greet").apply(new Parameter());
		Assertions.assertThat(result).isEqualTo("Hello stranger");
	}
}
