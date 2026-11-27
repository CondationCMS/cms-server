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
import com.condation.cms.maven.add.ExtensionType;
import com.condation.cms.maven.add.ExtensionTypes;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OtherTypesTest {

    @Test
    void should_have_all_expected_type_ids() {
        assertThat(ExtensionTypes.all().stream().map(ExtensionType::id).toList()).containsExactlyInAnyOrderElementsOf(List.of(
                "hook", "shortcode", "template-function", "template-component", "realtime", "api-handler",
                "http-handler", "http-routes", "routes", "template-model", "query-operator", "variant-selector",
                "backup-post-processing", "cache-provider", "markdown-renderer", "template-engine",
                "server-lifecycle", "server-hook", "module-lifecycle"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"backup-post-processing", "cache-provider", "markdown-renderer", "template-engine",
        "server-lifecycle", "server-hook"})
    void should_compile_when_default_values_used(String id) {
        assertCompiles(render(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"backup-post-processing", "cache-provider", "markdown-renderer", "template-engine",
        "server-lifecycle", "server-hook"})
    void should_compile_when_value_contains_quote_and_backslash(String id) {
        var type = type(id);

        assertCompiles(type.render(hostileSpec(type)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"cache-provider", "markdown-renderer", "template-engine"})
    void should_return_given_name_and_mark_rest_as_todo_when_provider(String id) {
        var source = render(id, "name", "my-name");

        assertThat(source).contains("return \"my-name\";").contains("UnsupportedOperationException(\"TODO\")");
    }

    @Test
    void should_extend_matching_extension_points() {
        assertThat(render("backup-post-processing")).contains("extends BackupFilePostProcessingExtensionPoint")
                .contains("postProcess(Path backupFile)");
        assertThat(render("cache-provider")).contains("extends CacheProviderExtensionPoint");
        assertThat(render("markdown-renderer")).contains("extends MarkdownRendererProviderExtensionPoint");
        assertThat(render("template-engine")).contains("extends TemplateEngineProviderExtensionPoint");
        assertThat(render("server-lifecycle")).contains("extends ServerLifecycleExtensionPoint")
                .contains("void started()").contains("void stopped()");
        assertThat(render("server-hook")).contains("extends ServerHookSystemRegisterExtensionPoint")
                .contains("register(HookSystem hookSystem)");
    }
}
