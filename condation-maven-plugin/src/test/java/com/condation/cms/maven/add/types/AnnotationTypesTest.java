package com.condation.cms.maven.add.types;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import static com.condation.cms.maven.add.types.TypeTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AnnotationTypesTest {

    private static final String IDS = "hook,shortcode,template-function,template-component,realtime,module-lifecycle";

    @ParameterizedTest
    @ValueSource(strings = {"hook", "shortcode", "template-function", "template-component", "realtime", "module-lifecycle"})
    void should_compile_when_default_values_used(String id) {
        assertCompiles(render(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hook", "shortcode", "template-function", "template-component", "realtime", "module-lifecycle"})
    void should_compile_when_value_contains_quote_and_backslash(String id) {
        var type = type(id);

        assertCompiles(type.render(hostileSpec(type)));
    }

    @Test
    void should_declare_package_and_class() {
        var source = render("shortcode");

        assertThat(source).contains("package com.example.mod;").contains("public class MyExt ");
    }

    @Test
    void should_contain_given_shortcode_name() {
        assertThat(render("shortcode", "name", "my-tag")).contains("@ShortCode(\"my-tag\")");
    }

    @Test
    void should_use_param_annotation_with_given_parameter_name() {
        assertThat(render("shortcode", "param", "title")).contains("@Param(\"title\") Object title");
        assertThat(render("template-function", "param", "title")).contains("@Param(\"title\") Object title");
        assertThat(render("template-component", "param", "title")).contains("@Param(\"title\") Object title");
    }

    @Test
    void should_declare_object_parameters_because_shortcodes_pass_numbers_and_booleans() {
        assertThat(render("shortcode")).doesNotContain("String name").contains("Object name");
        assertThat(render("template-function")).doesNotContain("String name").contains("Object name");
        assertThat(render("template-component")).doesNotContain("String name").contains("Object name");
        assertThat(render("hook", "kind", "filter")).doesNotContain("String value");
    }

    @Test
    void should_reject_relative_realtime_path_with_leading_slash_or_parent_reference() {
        assertThatThrownBy(() -> render("realtime", "path", "/events")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> render("realtime", "path", "a/../b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> render("realtime", "path", "a?b")).isInstanceOf(IllegalArgumentException.class);
        assertThat(render("realtime", "path", "chat/room")).contains("path = \"chat/room\"");
    }

    @Test
    void should_reject_invalid_parameter_name() {
        assertThatThrownBy(() -> render("shortcode", "param", "my param")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> render("template-function", "param", "class")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_contain_template_function_and_component_names() {
        assertThat(render("template-function", "name", "fn1")).contains("@TemplateFunction(\"fn1\")");
        assertThat(render("template-component", "name", "badge")).contains("@TemplateComponent(\"badge\")");
    }

    @Test
    void should_render_action_with_scope_when_kind_action() {
        var source = render("hook", "kind", "action", "hook", "my/hook", "scope", "APPLICATION");

        assertThat(source).contains("@Action(value = \"my/hook\", scope = Scope.APPLICATION)");
    }

    @Test
    void should_render_filter_when_kind_filter() {
        var source = render("hook", "kind", "filter", "hook", "my/filter");

        assertThat(source).contains("@Filter(value = \"my/filter\"").contains("(Object value)");
        assertCompiles(source);
    }

    @Test
    void should_render_sse_variant_by_default() {
        assertThat(render("realtime")).contains("RealtimeTransport.SSE").contains("SseConnection");
    }

    @Test
    void should_render_websocket_variant_when_transport_websocket() {
        var source = render("realtime", "transport", "WEBSOCKET", "path", "echo");

        assertThat(source).contains("RealtimeTransport.WEBSOCKET").contains("path = \"echo\"").contains("WebSocketConnection");
        assertCompiles(source);
    }

    @Test
    void should_extend_module_lifecycle_extension() {
        assertThat(render("module-lifecycle")).contains("@Extension(ModuleLifeCycleExtension.class)")
                .contains("extends ModuleLifeCycleExtension<Context>")
                .contains("void activate()").contains("void deactivate()");
    }

    @Test
    void should_register_every_type_of_this_task() {
        for (var id : IDS.split(",")) {
            assertThat(type(id).id()).isEqualTo(id);
            assertThat(type(id).label()).isNotBlank();
        }
    }
}
