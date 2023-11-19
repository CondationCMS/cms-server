package com.github.thmarx.cms.modules.thymeleaf;

/*-
 * #%L
 * thymeleaf-module
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
