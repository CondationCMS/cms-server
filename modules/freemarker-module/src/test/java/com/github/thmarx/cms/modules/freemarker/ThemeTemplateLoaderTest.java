package com.github.thmarx.cms.modules.freemarker;

/*-
 * #%L
 * freemarker-module
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

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.util.List;
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
public class ThemeTemplateLoaderTest {
	
	
	@Mock
	TemplateLoader siteTemplateLoader;
	@Mock
	TemplateLoader themeTemplateLoader;
	
	MultiTemplateLoader sut;
	
	@BeforeEach
	void setup () {
		sut = new MultiTemplateLoader(List.of(siteTemplateLoader, themeTemplateLoader).toArray(TemplateLoader[]::new));
	}
	

	@Test
	public void site_template() throws IOException {
		Mockito.when(siteTemplateLoader.findTemplateSource("test")).thenReturn("the template");
		
		sut.findTemplateSource("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(0)).findTemplateSource("test");
	}
	
	@Test
	public void theme_template() throws IOException {
		Mockito.when(siteTemplateLoader.findTemplateSource("test")).thenReturn(null);
		
		sut.findTemplateSource("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(1)).findTemplateSource("test");
	}
	
}
