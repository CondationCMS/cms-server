package com.condation.cms.templates.components;

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
import com.condation.cms.api.annotations.TemplateComponent;
import com.condation.cms.api.model.Parameter;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author t.marx
 */
class TemplateComponentsRegistrationTest {

    private TemplateComponents components;

    @BeforeEach
    void setup() {
        components = new TemplateComponents();
    }

    // --- context style (Parameter) ---

    @Test
    void context_style_default_namespace_is_registered() {
        components.register(new ContextStyleHandler());

        Assertions.assertThat(components.getComponentNames()).contains("ext:badge");
    }

    @Test
    void context_style_returns_correct_value() {
        components.register(new ContextStyleHandler());

        String result = components.execute("ext:badge", Map.of("label", "new"), null);
        Assertions.assertThat(result).isEqualTo("<span class='badge'>new</span>");
    }

    @Test
    void context_style_explicit_namespace_is_registered() {
        components.register(new ContextStyleHandler());

        Assertions.assertThat(components.getComponentNames()).contains("theme:card");
    }

    @Test
    void context_style_explicit_namespace_returns_correct_value() {
        components.register(new ContextStyleHandler());

        String result = components.execute("theme:card", Map.of("title", "My Title"), null);
        Assertions.assertThat(result).isEqualTo("<div class='card'>My Title</div>");
    }

    // --- @Param named-params style ---

    @Test
    void param_style_default_namespace_is_registered() {
        components.register(new ParamStyleHandler());

        Assertions.assertThat(components.getComponentNames()).contains("ext:alert");
    }

    @Test
    void param_style_returns_correct_value() {
        components.register(new ParamStyleHandler());

        String result = components.execute("ext:alert", Map.of("message", "Watch out!"), null);
        Assertions.assertThat(result).isEqualTo("<div class='alert'>Watch out!</div>");
    }

    @Test
    void param_style_multiple_params_returns_correct_value() {
        components.register(new ParamStyleHandler());

        String result = components.execute("ext:full", Map.of("first", "Hello", "second", "World"), null);
        Assertions.assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void param_style_explicit_namespace_is_registered() {
        components.register(new ParamStyleHandler());

        Assertions.assertThat(components.getComponentNames()).contains("ns1:item");
    }

    @Test
    void param_style_explicit_namespace_returns_correct_value() {
        components.register(new ParamStyleHandler());

        String result = components.execute("ns1:item", Map.of("value", "42"), null);
        Assertions.assertThat(result).isEqualTo("<li>42</li>");
    }

    // --- null / empty ---

    @Test
    void null_handler_is_ignored() {
        Assertions.assertThatNoException().isThrownBy(() -> components.register((Object) null));
        Assertions.assertThat(components.getComponentNames()).isEmpty();
    }

    @Test
    void handler_without_annotation_registers_nothing() {
        components.register(new NoAnnotationHandler());

        Assertions.assertThat(components.getComponentNames()).isEmpty();
    }

    // --- handler classes ---

    public static class ContextStyleHandler {
        @TemplateComponent("badge")
        public String badge(Parameter param) {
            return "<span class='badge'>%s</span>".formatted(param.getOrDefault("label", ""));
        }

        @TemplateComponent(value = "card", namespace = "theme")
        public String card(Parameter param) {
            return "<div class='card'>%s</div>".formatted(param.getOrDefault("title", ""));
        }
    }

    public static class ParamStyleHandler {
        @TemplateComponent("alert")
        public String alert(@Param("message") String message) {
            return "<div class='alert'>%s</div>".formatted(message);
        }

        @TemplateComponent("full")
        public String full(@Param("first") String first, @Param("second") String second) {
            return first + " " + second;
        }

        @TemplateComponent(value = "item", namespace = "ns1")
        public String item(@Param("value") String value) {
            return "<li>%s</li>".formatted(value);
        }
    }

    public static class NoAnnotationHandler {
        public String notAComponent(Parameter param) {
            return "ignored";
        }
    }
}
