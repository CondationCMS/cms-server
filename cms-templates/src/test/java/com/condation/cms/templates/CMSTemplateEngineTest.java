package com.condation.cms.templates;

/*-
 * #%L
 * cms-templates
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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

import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CMSTemplateEngineTest {

    private TemplateConfiguration configuration;
    private TemplateCache templateCache;
    private TemplateLoader templateLoader;
    private CMSTemplateEngine engine;

    @BeforeEach
    void setUp() {
        configuration = mock(TemplateConfiguration.class);
        templateCache = mock(TemplateCache.class);
        templateLoader = mock(TemplateLoader.class);

        when(configuration.isDevMode()).thenReturn(true); // Dev-Modus aktiv
        when(configuration.getTemplateCache()).thenReturn(templateCache);
        when(configuration.getTemplateLoader()).thenReturn(templateLoader);

        engine = new CMSTemplateEngine(configuration);
    }

    @Test
    void shouldNotUseCacheInDevMode() {
        // Arrange
        String templateName = "testTemplate";
        String templateContent = "Hello {{ name }}";
        when(templateLoader.load(templateName)).thenReturn(templateContent);

        // Act
        Template result = engine.getTemplate(templateName);

        // Assert
        assertThat(result).isNotNull();
        verify(templateCache, never()).contains(templateName);
        verify(templateCache, never()).get(templateName);
        verify(templateCache, never()).put(eq(templateName), any());
    }

    @Test
    void shouldThrowExceptionIfTemplateNotFound() {
        // Arrange
        String templateName = "missingTemplate";
        when(templateLoader.load(templateName)).thenReturn(null);

        // Assert
        assertThatThrownBy(() -> engine.getTemplate(templateName))
                .isInstanceOf(TemplateNotFoundException.class)
                .hasMessageContaining("template missingTemplate not found");
    }
}
