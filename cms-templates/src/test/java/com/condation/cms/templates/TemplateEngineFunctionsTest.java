package com.condation.cms.templates;

/*-
 * #%L
 * CMS Templates
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

import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.annotations.TemplateFunction;
import com.condation.cms.api.extensions.RegisterTemplateFunctionExtensionPoint;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.InjectorFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.model.Parameter;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.hooksystem.CMSHookSystem;
import com.condation.cms.hooksystem.extensions.TemplateHooks;
import com.condation.cms.templates.components.TemplateComponents;
import com.condation.cms.templates.loaders.StringTemplateLoader;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Injector;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author thmar
 */
public class TemplateEngineFunctionsTest extends AbstractTemplateEngineTest {

	static DynamicConfiguration dynamicConfiguration;

	@BeforeAll
	public void setupFunctions() {
		var requestContext = new RequestContext();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(new CMSHookSystem()));
		requestContext.add(TemplateHooks.class, new TemplateHooks(requestContext));

		var injectorMock = Mockito.mock(Injector.class);
		requestContext.add(InjectorFeature.class, new InjectorFeature(injectorMock));

		var moduleManagerMock = Mockito.mock(ModuleManager.class);
		requestContext.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManagerMock));

		Mockito.when(injectorMock.getInstance(ModuleManager.class)).thenReturn(moduleManagerMock);
		Mockito.when(moduleManagerMock.extensions(RegisterTemplateFunctionExtensionPoint.class))
				.thenReturn(List.of(new TestFunctions()));

		dynamicConfiguration = new DynamicConfiguration(new TemplateComponents(), requestContext);
	}

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("date", "{{ date() | date('YYYY') }}")
				// no-arg style
				.add("fn_noarg", "{{ ext.testfn3() }}")
				// context style (Parameter)
				.add("fn_context", "{{ ext.testfn2({}) }}")
				// @Param style — parameter passed as JEXL map (keys must be quoted in JEXL3)
				.add("fn_param", "{{ ext.testfn4({'name': 'World'}) }}")
				// explicit namespace
				.add("fn_namespace", "{{ ns1.shout({'text': 'hello'}) }}")
				// chained with filter
				.add("fn_chained", "{{ ext.testfn3() | upper }}");
	}

	// --- built-in date function ---

	@Test
	public void test_date() throws IOException {
		var year = new SimpleDateFormat("YYYY").format(new Date());
		Template simpleTemplate = SUT.getTemplate("date");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate()).isEqualToIgnoringWhitespace(year);
	}

	// --- no-arg style renders correctly ---

	@Test
	void test_noarg_function_renders() throws IOException {
		Template template = SUT.getTemplate("fn_noarg");
		Assertions.assertThat(template.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("hi I'm testfn3");
	}

	// --- context style (Parameter) renders correctly ---

	@Test
	void test_context_style_function_renders() throws IOException {
		Template template = SUT.getTemplate("fn_context");
		Assertions.assertThat(template.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("hi I'm testfn2");
	}

	// --- @Param style renders correctly ---

	@Test
	void test_param_style_function_renders() throws IOException {
		Template template = SUT.getTemplate("fn_param");
		Assertions.assertThat(template.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("hi World");
	}

	// --- explicit namespace renders correctly ---

	@Test
	void test_explicit_namespace_function_renders() throws IOException {
		Template template = SUT.getTemplate("fn_namespace");
		Assertions.assertThat(template.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("HELLO!");
	}

	// --- function result can be chained with filter ---

	@Test
	void test_function_result_chained_with_filter() throws IOException {
		Template template = SUT.getTemplate("fn_chained");
		Assertions.assertThat(template.evaluate(Map.of(), dynamicConfiguration))
				.isEqualToIgnoringWhitespace("HI I'M TESTFN3");
	}

	// --- registration checks ---

	@Test
	void all_functions_are_registered() {
		var tfs = dynamicConfiguration.templateFunctions();
		// testfn1 (map), testfn2 (Parameter), testfn3 (no-arg), testfn4 (@Param), shout (@Param, ns1)
		Assertions.assertThat(tfs).isNotNull().hasSize(5);
	}

	@Test
	void param_style_function_invocation_returns_correct_value() {
		var fn4 = dynamicConfiguration.templateFunctions().stream()
				.filter(f -> f.name().equals("testfn4")).findFirst();
		Assertions.assertThat(fn4).isPresent();
		Assertions.assertThat(fn4.get().invoke(new Parameter(Map.of("name", "World"))))
				.isEqualTo("hi World");
	}

	// --- test handler ---

	public static class TestFunctions extends RegisterTemplateFunctionExtensionPoint {

		@Override
		public Map<String, Function<Parameter, ?>> functions() {
			return Map.of("testfn1", (params) -> "hi I'm testfn1");
		}

		@Override
		public List<Object> functionDefinitions() {
			return List.of(this);
		}

		// context style
		@TemplateFunction("testfn2")
		public Object testfn2(Parameter params) {
			return "hi I'm testfn2";
		}

		// no-arg style
		@TemplateFunction("testfn3")
		public Object testfn3() {
			return "hi I'm testfn3";
		}

		// @Param style, default namespace
		@TemplateFunction("testfn4")
		public Object testfn4(@Param("name") String name) {
			return "hi " + name;
		}

		// @Param style, explicit namespace
		@TemplateFunction(value = "shout", namespace = "ns1")
		public Object shout(@Param("text") String text) {
			return text.toUpperCase() + "!";
		}
	}
}
