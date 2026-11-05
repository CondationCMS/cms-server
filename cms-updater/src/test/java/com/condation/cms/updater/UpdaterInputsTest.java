package com.condation.cms.updater;

/*-
 * #%L
 * CondationCMS Updater
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

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdaterInputsTest {
    @TempDir Path temporary;

    @Test
    void homeDefaultsToParentAndPackageIsRequired() {
        var options = UpdaterMain.parse(new String[]{"install", "--package", "update.zip"});

        assertEquals(Path.of("../"), options.home());
        assertEquals("update.zip", options.packageLocation());
        assertThrows(IllegalArgumentException.class, () -> UpdaterMain.parse(new String[]{"install"}));
    }

    @Test
    void readsVersionFromServerJarProperties() throws Exception {
        Path home = Files.createDirectory(temporary.resolve("home"));
        writeServerJar(home.resolve("cms-server-8.1.0.jar"), "8.2.3");

        assertEquals("8.2.3", new InstalledVersionResolver().resolve(home));
    }

    @Test
    void fallsBackToVersionInServerJarFilename() throws Exception {
        Path home = Files.createDirectory(temporary.resolve("home"));
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(home.resolve("cms-server-8.2.3.jar")))) {
            jar.finish();
        }

        assertEquals("8.2.3", new InstalledVersionResolver().resolve(home));
    }

    @Test
    void rejectsAmbiguousServerJars() throws Exception {
        Path home = Files.createDirectory(temporary.resolve("home"));
        writeServerJar(home.resolve("cms-server-8.2.0.jar"), "8.2.0");
        writeServerJar(home.resolve("cms-server-8.3.0.jar"), "8.3.0");

        var error = assertThrows(java.io.IOException.class, () -> new InstalledVersionResolver().resolve(home));
        assertTrue(error.getMessage().contains("multiple cms-server JARs"));
    }

    @Test
    void downloadsPackageFromHttpUrl() throws Exception {
        byte[] packageBytes = "update-package".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/update.zip", exchange -> {
            exchange.sendResponseHeaders(200, packageBytes.length);
            try (var body = exchange.getResponseBody()) { body.write(packageBytes); }
        });
        server.start();
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/update.zip";
            Path downloaded;
            try (UpdatePackageSource source = UpdatePackageSource.open(url)) {
                downloaded = source.path();
                assertEquals("update-package", Files.readString(downloaded));
            }
            assertTrue(Files.notExists(downloaded));
        } finally {
            server.stop(0);
        }
    }

    private static void writeServerJar(Path jarFile, String version) throws Exception {
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile))) {
            jar.putNextEntry(new JarEntry("com/condation/cms/application.properties"));
            jar.write(("name=cms-server\nversion=" + version + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jar.closeEntry();
        }
    }
}
