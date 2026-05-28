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
import com.condation.cms.api.annotations.TemplateFunction;
import com.condation.cms.api.model.Parameter;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author t.marx
 */
class TemplateFunctionsRegistrationTest {

    private TemplateFunctions functions;

    @BeforeEach
    void setup() {
        functions = new TemplateFunctions();
    }

    // --- no-arg style ---

    @Test
    void no_arg_style_default_namespace_is_registered() {
        functions.register(new NoArgHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("ext") && f.name().equals("hello"));
    }

    @Test
    void no_arg_style_returns_correct_value() {
        functions.register(new NoArgHandler());

        var fn = findFunction("ext", "hello");
        Assertions.assertThat(fn.function().apply(new Parameter())).isEqualTo("Hello World");
    }

    @Test
    void no_arg_style_explicit_namespace_is_registered() {
        functions.register(new NoArgHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("theme") && f.name().equals("version"));
    }

    @Test
    void no_arg_style_explicit_namespace_returns_correct_value() {
        functions.register(new NoArgHandler());

        var fn = findFunction("theme", "version");
        Assertions.assertThat(fn.function().apply(new Parameter())).isEqualTo("1.0.0");
    }

    // --- context style (Parameter) ---

    @Test
    void context_style_default_namespace_is_registered() {
        functions.register(new ContextStyleHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("ext") && f.name().equals("greet"));
    }

    @Test
    void context_style_returns_correct_value() {
        functions.register(new ContextStyleHandler());

        var fn = findFunction("ext", "greet");
        Assertions.assertThat(fn.function().apply(new Parameter(Map.of("name", "CondationCMS"))))
                .isEqualTo("Hello CondationCMS");
    }

    @Test
    void context_style_explicit_namespace_is_registered() {
        functions.register(new ContextStyleHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("ns1") && f.name().equals("upper"));
    }

    @Test
    void context_style_explicit_namespace_returns_correct_value() {
        functions.register(new ContextStyleHandler());

        var fn = findFunction("ns1", "upper");
        Assertions.assertThat(fn.function().apply(new Parameter(Map.of("text", "hello"))))
                .isEqualTo("HELLO");
    }

    // --- @Param named-params style ---

    @Test
    void param_style_default_namespace_is_registered() {
        functions.register(new ParamStyleHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("ext") && f.name().equals("add"));
    }

    @Test
    void param_style_returns_correct_value() {
        functions.register(new ParamStyleHandler());

        var fn = findFunction("ext", "add");
        Assertions.assertThat(fn.function().apply(new Parameter(Map.of("a", 3, "b", 4))))
                .isEqualTo(7);
    }

    @Test
    void param_style_single_param_returns_correct_value() {
        functions.register(new ParamStyleHandler());

        var fn = findFunction("ext", "shout");
        Assertions.assertThat(fn.function().apply(new Parameter(Map.of("text", "hello"))))
                .isEqualTo("HELLO!");
    }

    @Test
    void param_style_explicit_namespace_is_registered() {
        functions.register(new ParamStyleHandler());

        Assertions.assertThat(functions.getFunctions())
                .anyMatch(f -> f.namespace().equals("ns1") && f.name().equals("repeat"));
    }

    @Test
    void param_style_explicit_namespace_returns_correct_value() {
        functions.register(new ParamStyleHandler());

        var fn = findFunction("ns1", "repeat");
        Assertions.assertThat(fn.function().apply(new Parameter(Map.of("text", "hi", "times", 3))))
                .isEqualTo("hihihi");
    }

    // --- null / empty ---

    @Test
    void null_handler_is_ignored() {
        Assertions.assertThatNoException().isThrownBy(() -> functions.register((Object) null));
        Assertions.assertThat(functions.getFunctions()).isEmpty();
    }

    @Test
    void handler_without_annotation_registers_nothing() {
        functions.register(new NoAnnotationHandler());

        Assertions.assertThat(functions.getFunctions()).isEmpty();
    }

    // --- helper ---

    private FunctionMap.ExtFunction findFunction(String namespace, String name) {
        return functions.getFunctions().stream()
                .filter(f -> f.namespace().equals(namespace) && f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Function not found: " + namespace + ":" + name));
    }

    // --- handler classes ---

    public static class NoArgHandler {
        @TemplateFunction("hello")
        public String hello() {
            return "Hello World";
        }

        @TemplateFunction(value = "version", namespace = "theme")
        public String version() {
            return "1.0.0";
        }
    }

    public static class ContextStyleHandler {
        @TemplateFunction("greet")
        public String greet(Parameter param) {
            return "Hello " + param.getOrDefault("name", "");
        }

        @TemplateFunction(value = "upper", namespace = "ns1")
        public String upper(Parameter param) {
            return ((String) param.getOrDefault("text", "")).toUpperCase();
        }
    }

    public static class ParamStyleHandler {
        @TemplateFunction("add")
        public Integer add(@Param("a") Integer a, @Param("b") Integer b) {
            return a + b;
        }

        @TemplateFunction("shout")
        public String shout(@Param("text") String text) {
            return text.toUpperCase() + "!";
        }

        @TemplateFunction(value = "repeat", namespace = "ns1")
        public String repeat(@Param("text") String text, @Param("times") Integer times) {
            return text.repeat(times);
        }
    }

    public static class NoAnnotationHandler {
        public String notAFunction(Parameter param) {
            return "ignored";
        }
    }
}
