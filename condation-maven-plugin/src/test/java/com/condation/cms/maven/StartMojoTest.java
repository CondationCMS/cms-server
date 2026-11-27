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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StartMojoTest {

    @TempDir
    Path tmp;

    private StartMojo mojo(Path home) {
        var mojo = new StartMojo();
        mojo.cmsHome = home.toFile();
        mojo.installModule = true;
        mojo.startTimeout = 5;
        return mojo;
    }

    @Test
    void should_return_home_when_directory_exists() throws MojoExecutionException {
        assertThat(mojo(tmp).requireHome()).isEqualTo(tmp);
    }

    @Test
    void should_fail_with_path_when_home_missing() {
        var missing = tmp.resolve("cms-home");

        assertThatThrownBy(() -> mojo(missing).requireHome())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining(missing.toString())
                .hasMessageContaining("condation.home");
    }

    @Test
    void should_fail_before_anything_else_when_home_missing() {
        // repository system etc. are null: any step after the home check would NPE
        var mojo = mojo(tmp.resolve("missing"));

        assertThatThrownBy(mojo::execute)
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void should_do_nothing_when_skip_is_set() {
        var mojo = mojo(tmp.resolve("missing"));
        mojo.skip = true;

        assertThatCode(mojo::execute).doesNotThrowAnyException();
    }

    @Test
    void should_refuse_start_when_server_already_running() throws IOException {
        var handle = ProcessHandle.current();
        Files.writeString(tmp.resolve("cms.pid"), "pid=" + handle.pid()
                + "\nstartedAt=" + handle.info().startInstant().get().toEpochMilli() + "\n");

        assertThatThrownBy(() -> mojo(tmp).execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("condation:stop");
    }

    @Test
    void should_fail_with_clear_message_before_install_when_server_toml_missing() {
        // module zip is missing too: the server.toml error must win, i.e. be checked before the install
        assertThatThrownBy(() -> mojo(tmp).execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("server.toml");
    }

    @Test
    void should_fail_when_server_port_not_defined() throws IOException {
        Files.writeString(tmp.resolve("server.toml"), "[server]\nip = \"127.0.0.1\"\n");

        assertThatThrownBy(() -> mojo(tmp).execute())
                .isInstanceOf(MojoExecutionException.class)
                .hasMessageContaining("server.port");
    }
}
