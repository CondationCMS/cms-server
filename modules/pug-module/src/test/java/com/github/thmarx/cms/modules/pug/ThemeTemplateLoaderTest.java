package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
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
