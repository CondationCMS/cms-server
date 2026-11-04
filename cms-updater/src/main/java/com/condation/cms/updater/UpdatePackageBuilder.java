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

import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Builds a full-file update ZIP from a staged CMS distribution. */
public final class UpdatePackageBuilder {
    private static final String PRODUCT = "condationcms";

    public Path build(Path distributionHome, Path scriptsDirectory, Path output, String version, String minimumVersion)
            throws IOException {
        Path home = distributionHome.toAbsolutePath().normalize();
        if (!Files.isDirectory(home)) throw new IOException("Distribution home does not exist: " + home);
        Path outputFile = output.toAbsolutePath().normalize();
        List<PayloadFile> payload = new ArrayList<>();
        List<UpdateManifest.MigrationEntry> migrations = new ArrayList<>();

        try (var entries = Files.list(home)) {
            List<Path> serverJars = entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("cms-server-.*\\.jar"))
                    .toList();
            if (serverJars.size() != 1) throw new IOException("Expected exactly one versioned cms-server JAR in " + home);
            String expectedName = "cms-server-" + version + ".jar";
            if (!serverJars.get(0).getFileName().toString().equals(expectedName)) {
                throw new IOException("Expected " + expectedName + " but found " + serverJars.get(0).getFileName());
            }
            payload.add(payload(serverJars.get(0), serverJars.get(0).getFileName().toString()));
        }

        Path libraries = home.resolve("lib");
        if (!Files.isDirectory(libraries)) throw new IOException("Distribution is missing lib directory: " + libraries);
        try (var entries = Files.walk(libraries)) {
            for (Path file : entries.filter(Files::isRegularFile).sorted().toList()) {
                payload.add(payload(file, home.relativize(file).toString().replace('\\', '/')));
            }
        }

        for (String script : List.of("server.sh", "server.bat", "debug.sh", "debug.bat")) {
            Path file = scriptsDirectory.resolve(script);
            if (Files.isRegularFile(file)) payload.add(payload(file, script));
        }

        Path migrationsDirectory = home.resolve("migrations");
        if (Files.isDirectory(migrationsDirectory)) {
            try (var entries = Files.list(migrationsDirectory)) {
                for (Path file : entries.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".jar")).sorted().toList()) {
                    String name = file.getFileName().toString();
                    String id = name.substring(0, name.length() - 4);
                    migrations.add(new UpdateManifest.MigrationEntry(id, "migrations/" + name, sha256(file)));
                }
            }
        }

        List<UpdateManifest.UpdateFile> manifestFiles = payload.stream()
                .map(file -> new UpdateManifest.UpdateFile(file.entryName(), file.target(), file.sha256(), file.executable())).toList();
        UpdateManifest manifest = new UpdateManifest(PRODUCT, version, minimumVersion, manifestFiles,
                migrations, List.of("lib"), List.of("cms-server-*.jar"));
        Files.createDirectories(outputFile.getParent());
        try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(outputFile)) {
            put(zip, "manifest.json", new GsonBuilder().setPrettyPrinting().create().toJson(manifest).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            for (PayloadFile file : payload) putFile(zip, file.entryName(), file.source(), file.executable());
            for (UpdateManifest.MigrationEntry migration : migrations) {
                putFile(zip, migration.artifact(), migrationsDirectory.resolve(Path.of(migration.artifact()).getFileName()), false);
            }
        }
        return outputFile;
    }

    private static PayloadFile payload(Path file, String target) throws IOException {
        String normalizedTarget = target.replace('\\', '/');
        String source = "files/" + normalizedTarget;
        return new PayloadFile(file, source, normalizedTarget, sha256(file), isUnixExecutableScript(normalizedTarget));
    }

    private static boolean isUnixExecutableScript(String target) {
        return target.equals("server.sh") || target.equals("debug.sh");
    }

    private static String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var input = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void putFile(ZipArchiveOutputStream zip, String name, Path file, boolean executable) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        if (executable) entry.setUnixMode(0755);
        zip.putArchiveEntry(entry);
        try (var input = Files.newInputStream(file)) { input.transferTo(zip); }
        zip.closeArchiveEntry();
    }

    private static void put(ZipArchiveOutputStream zip, String name, byte[] bytes) throws IOException {
        zip.putArchiveEntry(new ZipArchiveEntry(name));
        zip.write(bytes);
        zip.closeArchiveEntry();
    }

    private record PayloadFile(Path source, String entryName, String target, String sha256, boolean executable) { }
}
