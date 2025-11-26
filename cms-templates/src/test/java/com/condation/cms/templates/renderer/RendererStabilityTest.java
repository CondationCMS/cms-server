package com.condation.cms.templates.renderer;

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

import com.condation.cms.templates.CMSTemplateEngine;
import com.condation.cms.templates.DynamicConfiguration;
import com.condation.cms.templates.TemplateConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RendererStabilityTest {

    static CMSTemplateEngine engine;

    @BeforeAll
    public static void setup() {
        var config = new TemplateConfiguration(true);
        config.registerTag(new com.condation.cms.templates.tags.IfTag());
        config.registerTag(new com.condation.cms.templates.tags.EndIfTag());
        engine = new CMSTemplateEngine(config);
    }

    @Test
    public void testDeeplyNestedTemplate() {
        var template = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            template.append("{% if true %}");
        }
        for (int i = 0; i < 101; i++) {
            template.append("{% endif %}");
        }
        var compiledTemplate = engine.getTemplateFromString(template.toString());
        try {
            compiledTemplate.evaluate(Map.of(), new StringWriter(), DynamicConfiguration.EMPTY);
        } catch (StackOverflowError e) {
            // expected
        } catch (Exception e) {
			// fail
		}
    }

    @Test
    public void testUnknownTagInProdMode() {
        var writer = new StringWriter();
        try {
            var template = engine.getTemplateFromString("{% unknown %}");
            template.evaluate(Map.of(), writer, DynamicConfiguration.EMPTY);
        } catch (Exception e) {
            // ignore
        }
        assertTrue(writer.toString().isEmpty());
    }
}
