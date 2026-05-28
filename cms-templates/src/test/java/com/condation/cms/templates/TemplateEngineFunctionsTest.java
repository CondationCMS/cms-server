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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author thmar
 */
public class TemplateEngineFunctionsTest extends AbstractTemplateEngineTest {

	@Override
	public TemplateLoader getLoader() {
		return new StringTemplateLoader()
				.add("date", "{{ date() | date('YYYY') }}");
		
	}
	
	@Test
	public void test_date() throws IOException {
		
		var year = new SimpleDateFormat("YYYY").format(new Date());
		
		Template simpleTemplate = SUT.getTemplate("date");
		Assertions.assertThat(simpleTemplate).isNotNull();
		Assertions.assertThat(simpleTemplate.evaluate()).isEqualToIgnoringWhitespace(year);
	}
	
	
	@Test
	void getFunctionsFromDynamicConig () {
		var requestContext = new RequestContext();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(new CMSHookSystem()));
		requestContext.add(TemplateHooks.class, new TemplateHooks(requestContext));
		
		var injectorMock = Mockito.mock(Injector.class);
		requestContext.add(InjectorFeature.class, new InjectorFeature(injectorMock));
		
		var moduleManagerMock = Mockito.mock(ModuleManager.class);
		requestContext.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManagerMock));
		
		Mockito.when(injectorMock.getInstance(ModuleManager.class)).thenReturn(moduleManagerMock);
		Mockito.when(moduleManagerMock.extensions(RegisterTemplateFunctionExtensionPoint.class)).thenReturn(
				List.of(new TestFunctions())
		);
		
		DynamicConfiguration dc = new DynamicConfiguration(new TemplateComponents(), requestContext);
		
		var tfs = dc.templateFunctions();
		
		Assertions.assertThat(tfs).isNotNull().hasSize(4);
	}
	
	public static class TestFunctions extends RegisterTemplateFunctionExtensionPoint {

		@Override
		public Map<String, Function<Parameter, ?>> functions() {
			return Map.of("testfn1", (params) -> {
				return "hi I'm testfn1";
			});
		}

		@Override
		public List<Object> functionDefinitions() {
			return List.of(this);
		}
		
		@TemplateFunction("testfn2")
		public Object testfn2 (Parameter params) {
			return "hi I'm testfn2";
		}
		
		@TemplateFunction("testfn3")
		public Object testfn3 () {
			return "hi I'm testfn3";
		}

		@TemplateFunction("testfn4")
		public Object testfn4 (@Param("name") String name) {
			return "hi " + name;
		}
	}

	@Test
	void getFunctionsFromDynamicConfig_includes_param_style () {
		var requestContext = new RequestContext();
		requestContext.add(HookSystemFeature.class, new HookSystemFeature(new CMSHookSystem()));
		requestContext.add(TemplateHooks.class, new TemplateHooks(requestContext));

		var injectorMock = Mockito.mock(Injector.class);
		requestContext.add(InjectorFeature.class, new InjectorFeature(injectorMock));

		var moduleManagerMock = Mockito.mock(ModuleManager.class);
		requestContext.add(ModuleManagerFeature.class, new ModuleManagerFeature(moduleManagerMock));

		Mockito.when(injectorMock.getInstance(ModuleManager.class)).thenReturn(moduleManagerMock);
		Mockito.when(moduleManagerMock.extensions(RegisterTemplateFunctionExtensionPoint.class)).thenReturn(
				List.of(new TestFunctions())
		);

		DynamicConfiguration dc = new DynamicConfiguration(new TemplateComponents(), requestContext);

		var tfs = dc.templateFunctions();

		// testfn1 (map), testfn2 (Parameter), testfn3 (no-arg), testfn4 (@Param)
		Assertions.assertThat(tfs).isNotNull().hasSize(4);

		var fn4 = tfs.stream().filter(f -> f.name().equals("testfn4")).findFirst();
		Assertions.assertThat(fn4).isPresent();
		Assertions.assertThat(fn4.get().invoke(new Parameter(Map.of("name", "World"))))
				.isEqualTo("hi World");
	}
}
