package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
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

import de.neuland.pug4j.template.TemplateLoader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.NoSuchFileException;
import java.util.Optional;
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
	
	ThemeTemplateLoader sut;
	
	@BeforeEach
	void setup () {
		sut = new ThemeTemplateLoader(siteTemplateLoader, Optional.of(themeTemplateLoader));
	}
	
	@Test
	void site_template_last_mod () throws IOException {
		Mockito.when(siteTemplateLoader.getLastModified("test")).thenReturn(10L);
		
		sut.getLastModified("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(0)).getLastModified("test");
	}
	
	@Test
	void site_template_reader () throws IOException {
		Mockito.when(siteTemplateLoader.getReader("test")).thenReturn(new StringReader("test template"));
		
		sut.getReader("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(0)).getReader("test");
	}
	
	@Test
	void theme_template_last_mod () throws IOException {
		Mockito.when(siteTemplateLoader.getLastModified("test")).thenThrow(NoSuchFileException.class);
		
		sut.getLastModified("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(1)).getLastModified("test");
	}
	
	@Test
	void theme_template_reader () throws IOException {
		Mockito.when(siteTemplateLoader.getReader("test")).thenThrow(NoSuchFileException.class);
		
		sut.getReader("test");
		
		Mockito.verify(themeTemplateLoader, Mockito.times(1)).getReader("test");
	}
}
