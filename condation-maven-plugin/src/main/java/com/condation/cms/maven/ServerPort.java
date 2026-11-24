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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.tomlj.Toml;

/**
 * Reads the http port from <code>&lt;home&gt;/server.toml</code> (<code>[server] port</code>).
 */
public final class ServerPort {

    private ServerPort() {
    }

    public static int read(Path cmsHome) throws IOException {
        var file = cmsHome.resolve("server.toml");
        if (!Files.isRegularFile(file)) {
            throw new IOException("server.toml not found: " + file);
        }
        var result = Toml.parse(file);
        if (result.hasErrors()) {
            throw new IOException("Cannot parse " + file + ": " + result.errors().get(0));
        }
        var port = result.getLong("server.port");
        if (port == null) {
            throw new IOException("server.port not defined in " + file);
        }
        return Math.toIntExact(port);
    }
}
