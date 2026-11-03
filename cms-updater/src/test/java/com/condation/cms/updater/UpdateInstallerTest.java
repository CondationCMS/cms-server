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

import com.google.gson.Gson;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateInstallerTest {
    private static final Gson GSON = new Gson();
    @TempDir Path temporary;

    @Test
    void installsFilesAndWritesVersionWithoutReplacingUnlistedFiles() throws Exception {
        Path installation = installation("8.2.0");
        Files.createDirectories(installation.resolve("config"));
        Files.writeString(installation.resolve("config/local.toml"), "keep me");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(new FileSpec("files/server.jar", "lib/server.jar", "new server", null)), List.of());

        UpdateResult result = new UpdateInstaller().install(installation, archive, "8.2.0");

        assertEquals("8.5.0", result.installedVersion());
        assertEquals("new server", Files.readString(installation.resolve("lib/server.jar")));
        assertEquals("keep me", Files.readString(installation.resolve("config/local.toml")));
        assertEquals("8.5.0", Files.readString(installation.resolve("data/system/cms-version")).trim());
        assertEquals("old server", Files.readString(installation.resolve("data/update-backups/8.5.0/lib/server.jar")));
    }

    @Test
    void rejectsAnInstallationOlderThanPackageMinimum() throws Exception {
        Path installation = installation("8.1.0");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(), List.of());

        IOException error = assertThrows(IOException.class,
                () -> new UpdateInstaller().install(installation, archive, "8.1.0"));

        assertTrue(error.getMessage().contains("at least version 8.2.0"));
    }

    @Test
    void rejectsPathTraversalBeforeChangingInstallation() throws Exception {
        Path installation = installation("8.2.0");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(new FileSpec("files/server.jar", "../escaped", "bad", null)), List.of());

        assertThrows(IllegalArgumentException.class,
                () -> new UpdateInstaller().install(installation, archive, "8.2.0"));
        assertEquals("old server", Files.readString(installation.resolve("lib/server.jar")));
    }

    @Test
    void rejectsFileWithInvalidChecksum() throws Exception {
        Path installation = installation("8.2.0");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(new FileSpec("files/server.jar", "lib/server.jar", "tampered", "0".repeat(64))), List.of());

        IOException error = assertThrows(IOException.class,
                () -> new UpdateInstaller().install(installation, archive, "8.2.0"));

        assertTrue(error.getMessage().contains("Checksum mismatch"));
        assertEquals("old server", Files.readString(installation.resolve("lib/server.jar")));
    }

    @Test
    void refusesToUpdateWhenCmsPidFileIdentifiesRunningProcess() throws Exception {
        Path installation = installation("8.2.0");
        long startedAt = ProcessHandle.current().info().startInstant().orElseThrow().toEpochMilli();
        Files.writeString(installation.resolve(ServerProcessChecker.PID_FILE_NAME),
                "pid=" + ProcessHandle.current().pid() + System.lineSeparator() + "startedAt=" + startedAt);
        Path archive = packageZip("8.5.0", "8.2.0", List.of(), List.of());

        IOException error = assertThrows(IOException.class,
                () -> new UpdateInstaller().install(installation, archive, "8.2.0"));

        assertTrue(error.getMessage().contains("server is running"));
        assertEquals("old server", Files.readString(installation.resolve("lib/server.jar")));
    }

    @Test
    void removesStalePidFileAndAllowsUpdate() throws Exception {
        Path installation = installation("8.2.0");
        Files.writeString(installation.resolve(ServerProcessChecker.PID_FILE_NAME), "pid=999999999\nstartedAt=1");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(), List.of());

        new UpdateInstaller().install(installation, archive, "8.2.0");

        assertTrue(Files.notExists(installation.resolve(ServerProcessChecker.PID_FILE_NAME)));
    }

    @Test
    void replacesLibraryDirectoryAndRemovesOldVersionedServerJar() throws Exception {
        Path installation = installation("8.2.0");
        Files.createDirectories(installation.resolve("lib"));
        Files.writeString(installation.resolve("lib/old-library.jar"), "old library");
        Files.writeString(installation.resolve("cms-server-8.2.0.jar"), "old server jar");
        Path archive = packageZip("8.5.0", "8.2.0", List.of(
                new FileSpec("files/cms-server-8.5.0.jar", "cms-server-8.5.0.jar", "new server", null),
                new FileSpec("files/lib/new-library.jar", "lib/new-library.jar", "new library", null)),
                List.of(), List.of("lib"), List.of("cms-server-*.jar"));

        new UpdateInstaller().install(installation, archive, "8.2.0");

        assertEquals("new library", Files.readString(installation.resolve("lib/new-library.jar")));
        assertTrue(Files.notExists(installation.resolve("lib/old-library.jar")));
        assertTrue(Files.notExists(installation.resolve("cms-server-8.2.0.jar")));
        assertEquals("old library", Files.readString(installation.resolve("data/update-backups/8.5.0/lib/old-library.jar")));
    }

    @Test
    void executesMigrationsInManifestOrderAndRecordsThem() throws Exception {
        Path installation = installation("8.2.0");
        Path archive = temporary.resolve("migrations.zip");
        String first = FirstMigration.class.getName();
        String second = SecondMigration.class.getName();
        byte[] firstJar = providerJar(first);
        byte[] secondJar = providerJar(second);
        UpdateManifest manifest = new UpdateManifest("condationcms", "8.5.0", "8.2.0", List.of(), List.of(
                new UpdateManifest.MigrationEntry("first", "migrations/first.jar", sha256(firstJar)),
                new UpdateManifest.MigrationEntry("second", "migrations/second.jar", sha256(secondJar))));
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
            put(output, "manifest.json", GSON.toJson(manifest));
            put(output, "migrations/first.jar", firstJar);
            put(output, "migrations/second.jar", secondJar);
        }

        UpdateResult result = new UpdateInstaller().install(installation, archive, "8.2.0");

        assertEquals(List.of("first", "second"), result.migrationsExecuted());
        assertEquals("first\nsecond\n", Files.readString(installation.resolve("data/system/migration-order")));
        assertTrue(Files.readString(installation.resolve("data/system/update-history.json")).contains("first"));
    }

    private Path installation(String version) throws IOException {
        Path root = Files.createDirectory(temporary.resolve("installation"));
        Files.createDirectories(root.resolve("lib"));
        Files.createDirectories(root.resolve("data/system"));
        Files.writeString(root.resolve("lib/server.jar"), "old server");
        Files.writeString(root.resolve("data/system/cms-version"), version);
        return root;
    }

    private Path packageZip(String version, String minimumVersion, List<FileSpec> files,
            List<UpdateManifest.MigrationEntry> migrations) throws Exception {
        return packageZip(version, minimumVersion, files, migrations, List.of(), List.of());
    }

    private Path packageZip(String version, String minimumVersion, List<FileSpec> files,
            List<UpdateManifest.MigrationEntry> migrations, List<String> replaceDirectories,
            List<String> removePatterns) throws Exception {
        Path zip = temporary.resolve("update-" + version + ".zip");
        java.util.ArrayList<UpdateManifest.UpdateFile> manifestFiles = new java.util.ArrayList<>();
        for (FileSpec file : files) {
            manifestFiles.add(new UpdateManifest.UpdateFile(file.source, file.target,
                    file.checksum == null ? sha256(file.content) : file.checksum));
        }
        UpdateManifest manifest = new UpdateManifest("condationcms", version, minimumVersion, manifestFiles,
                migrations, replaceDirectories, removePatterns);
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            put(output, "manifest.json", GSON.toJson(manifest));
            for (FileSpec file : files) put(output, file.source, file.content);
        }
        return zip;
    }

    private static String sha256(String value) throws Exception {
        return sha256(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String sha256(byte[] value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
    }

    private static void put(ZipOutputStream zip, String path, String value) throws IOException {
        put(zip, path, value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void put(ZipOutputStream zip, String path, byte[] value) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(value);
        zip.closeEntry();
    }

    private static byte[] providerJar(String provider) throws IOException {
        String classPath = provider.replace('.', '/') + ".class";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JarOutputStream jar = new JarOutputStream(bytes)) {
            jar.putNextEntry(new JarEntry("META-INF/services/" + Migration.class.getName()));
            jar.write((provider + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jar.closeEntry();
            jar.putNextEntry(new JarEntry(classPath));
            try (InputStream classBytes = UpdateInstallerTest.class.getClassLoader().getResourceAsStream(classPath)) {
                if (classBytes == null) throw new IOException("Test migration class missing: " + classPath);
                classBytes.transferTo(jar);
            }
            jar.closeEntry();
        }
        return bytes.toByteArray();
    }

    public static class FirstMigration implements Migration {
        public String id() { return "first"; }
        public void execute(MigrationContext context) throws Exception {
            Path file = context.resolve("data/system/migration-order");
            Files.createDirectories(file.getParent());
            Files.writeString(file, "first\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        }
    }

    public static class SecondMigration implements Migration {
        public String id() { return "second"; }
        public void execute(MigrationContext context) throws Exception {
            Path file = context.resolve("data/system/migration-order");
            Files.createDirectories(file.getParent());
            Files.writeString(file, "second\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        }
    }

    private record FileSpec(String source, String target, String content, String checksum) { }
}
