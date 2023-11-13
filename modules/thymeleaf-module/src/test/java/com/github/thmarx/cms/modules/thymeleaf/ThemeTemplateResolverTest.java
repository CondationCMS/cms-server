package com.github.thmarx.cms.modules.thymeleaf;

/*-
 * #%L
 * thymeleaf-module
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

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class ThemeTemplateResolverTest {
	
	@Mock
	private ITemplateResolver siteTemplateResolver;
	@Mock
	private ITemplateResolver themeTemplateResolver;
	
	private ThemeTemplateResolver sut;
	
	@BeforeEach
	void setup () {
		sut = new ThemeTemplateResolver(siteTemplateResolver, Optional.ofNullable(themeTemplateResolver));
	}
	
	@Test
	public void template_from_theme() {
		Mockito.when(
				siteTemplateResolver.resolveTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(null)
				;
		
		sut.resolveTemplate(null, null, null, null);
		
		Mockito.verify(themeTemplateResolver, Mockito.times(1))
				.resolveTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}
	
	@Test
	public void template_from_site() {
		
		var templateResource = Mockito.mock(ITemplateResource.class);
		var validity = Mockito.mock(ICacheEntryValidity.class);
		
		var templateResolution = new TemplateResolution(templateResource, TemplateMode.HTML, validity);
		
		Mockito.when(templateResource.exists()).thenReturn(Boolean.TRUE);
		
		Mockito.when(
				siteTemplateResolver.resolveTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(templateResolution);
		
		sut.resolveTemplate(null, null, null, null);
		
		Mockito.verify(themeTemplateResolver, Mockito.times(0))
				.resolveTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
	}
	
}
