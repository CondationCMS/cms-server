package com.condation.cms.maven;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.condation.cms.maven.add.PromptUnavailableException;
import com.condation.cms.maven.add.Prompter;
import com.condation.cms.maven.add.UnavailablePrompter;
import com.condation.cms.maven.add.UserAbortException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AddMojoTest {

    @TempDir
    Path tmp;

    /** Answers prompts in call order; a null answer accepts the default. */
    static class FakePrompter implements Prompter {

        final Queue<String> answers = new ArrayDeque<>();
        final List<String> prompts = new ArrayList<>();
        final java.util.Map<String, Predicate<String>> validators = new java.util.HashMap<>();
        boolean confirmAnswer;
        boolean abort;

        FakePrompter answer(String... values) {
            for (var v : values) {
                answers.add(v == null ? "\u0000" : v);
            }
            return this;
        }

        private String next(String defaultValue) {
            if (abort) {
                throw new UserAbortException();
            }
            var value = answers.poll();
            if (value == null) {
                throw new AssertionError("unexpected prompt, no answer left");
            }
            return value.equals("\u0000") ? defaultValue : value;
        }

        @Override
        public String select(String label, List<String> choices, String defaultValue) {
            prompts.add("select:" + label);
            return next(defaultValue);
        }

        @Override
        public String ask(String label, String defaultValue, Predicate<String> validator, String errorMessage) {
            prompts.add("ask:" + label);
            validators.put(label, validator);
            return next(defaultValue);
        }

        @Override
        public boolean confirm(String label, boolean defaultValue) {
            prompts.add("confirm:" + label);
            return confirmAnswer;
        }
    }

    private AddMojo mojo(Prompter prompter) {
        var mojo = new AddMojo();
        mojo.sourceDirectory = tmp.resolve("src/main/java").toFile();
        mojo.groupId = "com.example.mod";
        mojo.prompter = prompter;
        return mojo;
    }

    private Path file(String relative) {
        return tmp.resolve("src/main/java").resolve(relative);
    }

    @Test
    void should_create_shortcode_class_when_all_values_given_as_properties() throws Exception {
        var prompter = new FakePrompter();
        var mojo = mojo(prompter);
        mojo.addType = "shortcode";
        mojo.addName = "HelloTag";
        mojo.addPackage = "com.example.mod";
        mojo.addOptions = "name=hi,param=title";

        mojo.execute();

        assertThat(file("com/example/mod/HelloTag.java")).content()
                .contains("@ShortCode(\"hi\")").contains("@Param(\"title\") Object title");
        assertThat(prompter.prompts).isEmpty();
    }

    @Test
    void should_ask_for_missing_values_with_prompter() throws Exception {
        var prompter = new FakePrompter().answer("hook", "MyHooks", "com.my.pkg", "filter", "my/filter", "APPLICATION");
        var mojo = mojo(prompter);

        mojo.execute();

        assertThat(file("com/my/pkg/MyHooks.java")).content()
                .contains("@Filter(value = \"my/filter\", scope = Scope.APPLICATION)");
        assertThat(prompter.prompts).containsExactly("select:Extension type", "ask:Class name", "ask:Package",
                "select:Kind of hook", "ask:Hook name", "select:Scope");
    }

    @Test
    void should_use_defaults_when_user_accepts_them() throws Exception {
        Files.createDirectories(file("com/found"));
        Files.writeString(file("com/found/Existing.java"), "package com.found;\n@Extension(X.class) class Existing {}");
        var prompter = new FakePrompter().answer((String) null, null, null, null, null);
        var mojo = mojo(prompter);
        mojo.addType = "shortcode";

        mojo.execute();

        assertThat(file("com/found/MyShortcodeExtension.java")).content().contains("@ShortCode(\"hello\")");
    }

    @Test
    void should_fail_with_property_hint_when_no_terminal_and_value_missing() {
        var mojo = mojo(new UnavailablePrompter());

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("condation.add.type");
    }

    @Test
    void should_fail_with_name_hint_when_only_name_missing() {
        var mojo = mojo(new UnavailablePrompter());
        mojo.addType = "shortcode";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("condation.add.name");
    }

    @Test
    void should_fail_with_option_hint_when_option_missing() {
        var mojo = mojo(new UnavailablePrompter());
        mojo.addType = "shortcode";
        mojo.addName = "X";
        mojo.addPackage = "a.b";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("condation.add.options").hasMessageContaining("name");
    }

    @Test
    void should_fail_when_unknown_type() {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "nope";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("nope").hasMessageContaining("shortcode");
    }

    @Test
    void should_fail_when_choice_value_invalid() {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "api-handler";
        mojo.addName = "Api";
        mojo.addPackage = "a.b";
        mojo.addOptions = "path=/x,method=FOO";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("FOO").hasMessageContaining("GET");
        assertThat(file("a/b/Api.java")).doesNotExist();
    }

    @Test
    void should_accept_choice_regardless_of_case() throws Exception {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "api-handler";
        mojo.addName = "Api";
        mojo.addPackage = "a.b";
        mojo.addOptions = "path=/x,method=post";

        mojo.execute();

        assertThat(file("a/b/Api.java")).content().contains("\"POST\"");
    }

    @Test
    void should_fail_when_class_name_property_invalid() {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "shortcode";
        mojo.addName = "1Bad";
        mojo.addPackage = "a.b";
        mojo.addOptions = "name=x,param=p";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class).hasMessageContaining("1Bad");
    }

    @Test
    void should_fail_when_unknown_option_key() {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "shortcode";
        mojo.addName = "X";
        mojo.addPackage = "a.b";
        mojo.addOptions = "nmae=x";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("nmae").hasMessageContaining("name");
    }

    @Test
    void should_parse_options_with_multiple_pairs_and_equals_in_value() throws Exception {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "realtime";
        mojo.addName = "Rt";
        mojo.addPackage = "a.b";
        mojo.addOptions = "transport=websocket,path=a=b";

        mojo.execute();

        assertThat(file("a/b/Rt.java")).content().contains("RealtimeTransport.WEBSOCKET").contains("path = \"a=b\"");
    }

    @Test
    void should_not_overwrite_when_user_declines_confirmation() throws Exception {
        Files.createDirectories(file("a/b"));
        Files.writeString(file("a/b/X.java"), "mine");
        var prompter = new FakePrompter();
        var mojo = mojo(prompter);
        mojo.addType = "module-lifecycle";
        mojo.addName = "X";
        mojo.addPackage = "a.b";

        assertThatCode(mojo::execute).doesNotThrowAnyException();

        assertThat(file("a/b/X.java")).hasContent("mine");
        assertThat(prompter.prompts).containsExactly("confirm:a/b/X.java already exists, overwrite?");
    }

    @Test
    void should_overwrite_when_user_confirms() throws Exception {
        Files.createDirectories(file("a/b"));
        Files.writeString(file("a/b/X.java"), "mine");
        var prompter = new FakePrompter();
        prompter.confirmAnswer = true;
        var mojo = mojo(prompter);
        mojo.addType = "module-lifecycle";
        mojo.addName = "X";
        mojo.addPackage = "a.b";

        mojo.execute();

        assertThat(file("a/b/X.java")).content().contains("class X");
    }

    @Test
    void should_overwrite_without_asking_when_overwrite_property_true() throws Exception {
        Files.createDirectories(file("a/b"));
        Files.writeString(file("a/b/X.java"), "mine");
        var prompter = new FakePrompter();
        var mojo = mojo(prompter);
        mojo.addType = "module-lifecycle";
        mojo.addName = "X";
        mojo.addPackage = "a.b";
        mojo.addOverwrite = true;

        mojo.execute();

        assertThat(file("a/b/X.java")).content().contains("class X");
        assertThat(prompter.prompts).isEmpty();
    }

    @Test
    void should_fail_with_overwrite_hint_when_file_exists_and_no_terminal() throws IOException {
        Files.createDirectories(file("a/b"));
        Files.writeString(file("a/b/X.java"), "mine");
        var mojo = mojo(new UnavailablePrompter());
        mojo.addType = "module-lifecycle";
        mojo.addName = "X";
        mojo.addPackage = "a.b";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("condation.add.overwrite");
        assertThat(file("a/b/X.java")).hasContent("mine");
    }

    @Test
    void should_abort_without_file_when_user_aborts() {
        var prompter = new FakePrompter();
        prompter.abort = true;
        var mojo = mojo(prompter);

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class).hasMessageContaining("Aborted");
        assertThat(tmp.resolve("src")).doesNotExist();
    }

    @Test
    void should_do_nothing_when_add_skip_set() {
        var mojo = mojo(new FakePrompter());
        mojo.addSkip = true;

        assertThatCode(mojo::execute).doesNotThrowAnyException();
        assertThat(tmp.resolve("src")).doesNotExist();
    }

    @Test
    void should_ignore_server_skip_flag() throws Exception {
        var mojo = mojo(new FakePrompter());
        mojo.skip = true;
        mojo.addType = "module-lifecycle";
        mojo.addName = "X";
        mojo.addPackage = "a.b";

        mojo.execute();

        assertThat(file("a/b/X.java")).exists();
    }

    @Test
    void should_signal_unavailable_prompter_with_dedicated_exception() {
        var unavailable = new UnavailablePrompter();

        assertThatThrownBy(() -> unavailable.ask("x", "y", s -> true, "bad"))
                .isInstanceOf(PromptUnavailableException.class);
    }

    @Test
    void should_validate_parameter_name_and_paths_while_asking() throws Exception {
        var prompter = new FakePrompter().answer("shortcode", "T1", "a.b", "hello", "title");
        mojo(prompter).execute();

        assertThat(prompter.validators.get("Parameter name").test("my param")).isFalse();
        assertThat(prompter.validators.get("Parameter name").test("class")).isFalse();
        assertThat(prompter.validators.get("Parameter name").test("title")).isTrue();

        var apiPrompter = new FakePrompter().answer("api-handler", "T2", "a.b", "/x", "GET");
        mojo(apiPrompter).execute();

        assertThat(apiPrompter.validators.get("API path").test("x")).isFalse();
        assertThat(apiPrompter.validators.get("API path").test("/x")).isTrue();
        assertThat(apiPrompter.validators.get("Class name").test("1Bad")).isFalse();
    }

    @Test
    void should_fail_with_hint_when_path_property_invalid() {
        var mojo = mojo(new FakePrompter());
        mojo.addType = "api-handler";
        mojo.addName = "Api";
        mojo.addPackage = "a.b";
        mojo.addOptions = "path=my-api,method=GET";

        assertThatThrownBy(mojo::execute).isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("path").hasMessageContaining("/");
        assertThat(file("a/b/Api.java")).doesNotExist();
    }

    @Test
    void should_not_prompt_in_batch_mode_even_when_terminal_available() {
        var session = org.mockito.Mockito.mock(org.apache.maven.execution.MavenSession.class);
        var request = new org.apache.maven.execution.DefaultMavenExecutionRequest();
        request.setInteractiveMode(false);
        org.mockito.Mockito.when(session.getRequest()).thenReturn(request);
        var mojo = mojo(null);
        mojo.session = session;

        assertThat(mojo.canPrompt(true)).isFalse();
    }

    @Test
    void should_prompt_when_terminal_available_and_interactive_mode() {
        var session = org.mockito.Mockito.mock(org.apache.maven.execution.MavenSession.class);
        org.mockito.Mockito.when(session.getRequest()).thenReturn(new org.apache.maven.execution.DefaultMavenExecutionRequest());
        var mojo = mojo(null);
        mojo.session = session;

        assertThat(mojo.canPrompt(true)).isTrue();
        assertThat(mojo.canPrompt(false)).isFalse();
    }

    @Test
    void should_skip_pom_projects() {
        var prompter = new FakePrompter();
        var mojo = mojo(prompter);
        mojo.packaging = "pom";

        assertThatCode(mojo::execute).doesNotThrowAnyException();
        assertThat(prompter.prompts).isEmpty();
        assertThat(tmp.resolve("src")).doesNotExist();
    }
}
