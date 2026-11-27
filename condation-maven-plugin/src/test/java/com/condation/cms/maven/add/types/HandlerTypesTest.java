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

class HandlerTypesTest {

    private static final String[] IDS = {"api-handler", "http-handler", "http-routes", "routes", "template-model",
        "query-operator", "variant-selector"};

    @ParameterizedTest
    @ValueSource(strings = {"api-handler", "http-handler", "http-routes", "routes", "template-model", "query-operator", "variant-selector"})
    void should_compile_when_default_values_used(String id) {
        assertCompiles(render(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"api-handler", "http-handler", "http-routes", "routes", "template-model", "query-operator", "variant-selector"})
    void should_compile_when_value_contains_quote_and_backslash(String id) {
        var type = type(id);

        assertCompiles(type.render(hostileSpec(type)));
    }

    @Test
    void should_register_every_type_of_this_task() {
        for (var id : IDS) {
            assertThat(type(id).id()).isEqualTo(id);
            assertThat(type(id).label()).isNotBlank();
        }
    }

    @Test
    void should_contain_path_and_method_when_api_handler() {
        var source = render("api-handler", "path", "/hello-api", "method", "POST");

        assertThat(source).contains("PathSpec.from(\"/hello-api\")").contains("\"POST\"")
                .contains("extends APIHandlerExtensionPoint");
    }

    @Test
    void should_contain_path_when_http_handler_and_routes() {
        assertThat(render("http-handler", "path", "/x")).contains("PathSpec.from(\"/x\")")
                .contains("extends HttpHandlerExtensionPoint");
        assertThat(render("http-routes", "path", "/y")).contains("PathSpec.from(\"/y\")")
                .contains("extends HttpRoutesExtensionPoint");
    }

    @Test
    void should_contain_route_annotation_with_method_when_routes() {
        assertThat(render("routes", "path", "/r", "method", "DELETE"))
                .contains("@Route(value = \"/r\", method = \"DELETE\")").contains("extends RoutesExtensionPoint");
    }

    @Test
    void should_contain_key_when_template_model() {
        assertThat(render("template-model", "key", "shop")).contains("Map.of(\"shop\"");
    }

    @Test
    void should_contain_operator_when_query_operator() {
        assertThat(render("query-operator", "operator", "startswith")).contains("return \"startswith\";");
    }

    @Test
    void should_contain_id_and_label_when_variant_selector() {
        var source = render("variant-selector", "id", "ab-test", "label", "A/B test");

        assertThat(source).contains("return \"ab-test\";").contains("return \"A/B test\";")
                .contains("VariantSelection.canonical()");
    }

    @ParameterizedTest
    @ValueSource(strings = {"api-handler", "http-handler", "http-routes", "routes"})
    void should_reject_path_without_leading_slash(String id) {
        assertThatThrownBy(() -> render(id, "path", "my-path")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("my-path");
    }
}
