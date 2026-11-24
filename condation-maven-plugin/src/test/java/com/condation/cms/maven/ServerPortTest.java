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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerPortTest {

    @TempDir
    Path home;

    @Test
    void should_return_port_when_server_toml_has_server_port() throws IOException {
        Files.writeString(home.resolve("server.toml"), "[server]\nport = 2020\nip = \"127.0.0.1\"\n");

        assertThat(ServerPort.read(home)).isEqualTo(2020);
    }

    @Test
    void should_fail_when_server_toml_missing() {
        assertThatThrownBy(() -> ServerPort.read(home))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("server.toml");
    }

    @Test
    void should_fail_when_port_not_defined() throws IOException {
        Files.writeString(home.resolve("server.toml"), "[server]\nip = \"127.0.0.1\"\n");

        assertThatThrownBy(() -> ServerPort.read(home))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("server.port");
    }
}
