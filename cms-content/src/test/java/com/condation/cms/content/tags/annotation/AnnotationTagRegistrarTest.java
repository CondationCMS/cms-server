package com.condation.cms.content.tags.annotation;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.content.shortcodes.annotation.AnnotationShortCodeRegistrar;
import com.condation.cms.api.Constants;
import com.condation.cms.api.annotations.Param;
import com.condation.cms.api.model.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.condation.cms.api.annotations.ShortCode;

/**
 * @author t.marx
 */
class AnnotationTagRegistrarTest {

    private AnnotationShortCodeRegistrar registrar;
    private Map<String, Function<Parameter, String>> tagMap;

    @BeforeEach
    void setup() {
        registrar = new AnnotationShortCodeRegistrar();
        tagMap = new HashMap<>();
    }

    // --- key building ---

    @Test
    void tag_with_default_namespace_uses_ext_prefix() {
        registrar.register(new DefaultNamespaceHandler(), tagMap);

        Assertions.assertThat(tagMap)
                .containsKey(Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE + ":hello");
    }

    @Test
    void tag_with_explicit_namespace_uses_that_prefix() {
        registrar.register(new CustomNamespaceHandler(), tagMap);

        Assertions.assertThat(tagMap).containsKey("ns1:greet");
    }

    // --- invocation ---

    @Test
    void tag_method_is_called_and_returns_correct_value() {
        registrar.register(new DefaultNamespaceHandler(), tagMap);

        String key = Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE + ":hello";
        Parameter param = new Parameter(Map.of("name", "World"));
        String result = tagMap.get(key).apply(param);

        Assertions.assertThat(result).isEqualTo("Hello World");
    }

    @Test
    void tag_with_explicit_namespace_invocation_returns_correct_value() {
        registrar.register(new CustomNamespaceHandler(), tagMap);

        Parameter param = new Parameter(Map.of("firstName", "Max", "lastName", "Mustermann"));
        String result = tagMap.get("ns1:greet").apply(param);

        Assertions.assertThat(result).isEqualTo("Max Mustermann");
    }

    // --- multiple tags on one handler ---

    @Test
    void multiple_tag_methods_on_same_handler_all_registered() {
        registrar.register(new MultiTagHandler(), tagMap);

        Assertions.assertThat(tagMap)
                .containsKey("ext:tagA")
                .containsKey("ext:tagB");
    }

    @Test
    void multiple_tag_methods_each_produce_correct_output() {
        registrar.register(new MultiTagHandler(), tagMap);

        Parameter param = new Parameter();
        Assertions.assertThat(tagMap.get("ext:tagA").apply(param)).isEqualTo("A");
        Assertions.assertThat(tagMap.get("ext:tagB").apply(param)).isEqualTo("B");
    }

    // --- null / empty handler ---

    @Test
    void null_handler_is_ignored_without_exception() {
        Assertions.assertThatNoException().isThrownBy(() -> registrar.register(null, tagMap));
        Assertions.assertThat(tagMap).isEmpty();
    }

    @Test
    void handler_without_tag_annotation_registers_nothing() {
        registrar.register(new NoTagHandler(), tagMap);

        Assertions.assertThat(tagMap).isEmpty();
    }

    // --- @Param named-params style ---

    @Test
    void tag_with_named_params_is_registered() {
        registrar.register(new NamedParamHandler(), tagMap);

        Assertions.assertThat(tagMap).containsKey("ext:greet2");
    }

    @Test
    void tag_with_named_params_receives_correct_values() {
        registrar.register(new NamedParamHandler(), tagMap);

        Parameter param = new Parameter(Map.of("firstName", "Max", "lastName", "Mustermann"));
        String result = tagMap.get("ext:greet2").apply(param);

        Assertions.assertThat(result).isEqualTo("Max Mustermann");
    }

    @Test
    void tag_with_named_params_returns_null_string_for_missing_attribute() {
        registrar.register(new NamedParamHandler(), tagMap);

        Parameter param = new Parameter();  // no attributes
        String result = tagMap.get("ext:greet2").apply(param);

        Assertions.assertThat(result).isEqualTo("null null");
    }

    // --- default parameter value ---

    @Test
    void tag_uses_getOrDefault_when_attribute_is_missing() {
        registrar.register(new DefaultNamespaceHandler(), tagMap);

        String key = Constants.TemplateNamespaces.DEFAULT_MODULE_NAMESPACE + ":hello";
        Parameter param = new Parameter();  // no "name" attribute
        String result = tagMap.get(key).apply(param);

        Assertions.assertThat(result).isEqualTo("Hello ");
    }

    // --- handler classes ---

    public static class DefaultNamespaceHandler {
        @ShortCode("hello")
        public String hello(Parameter param) {
            return "Hello " + param.getOrDefault("name", "");
        }
    }

    public static class CustomNamespaceHandler {
        @ShortCode(value = "greet", namespace = "ns1")
        public String greet(Parameter param) {
            return param.getOrDefault("firstName", "") + " " + param.getOrDefault("lastName", "");
        }
    }

    public static class MultiTagHandler {
        @ShortCode("tagA")
        public String tagA(Parameter param) {
            return "A";
        }

        @ShortCode("tagB")
        public String tagB(Parameter param) {
            return "B";
        }
    }

    public static class NoTagHandler {
        public String notATag(Parameter param) {
            return "ignored";
        }
    }

    public static class NamedParamHandler {
        @ShortCode("greet2")
        public String greet2(@Param("firstName") String firstName, @Param("lastName") String lastName) {
            return firstName + " " + lastName;
        }
    }
}
