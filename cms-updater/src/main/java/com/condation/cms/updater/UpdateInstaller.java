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
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Applies a CMS update ZIP to an offline installation. */
public final class UpdateInstaller {
    private static final String PRODUCT = "condationcms";
    private static final Gson GSON = new Gson();

    public UpdateResult install(Path installationDirectory, Path updatePackage, String installedVersion)
            throws IOException {
        Path installation = installationDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(installation)) {
            throw new IOException("Installation directory does not exist: " + installation);
        }
        new ServerProcessChecker().ensureStopped(installation);
        try (ZipFile zip = new ZipFile(updatePackage.toFile())) {
            UpdateManifest manifest = readManifest(zip);
            validateManifest(manifest, installedVersion);
            Path staging = UpdaterTempFiles.createDirectory("condation-update-");
            List<StagedFile> stagedFiles = new ArrayList<>();
            try {
                stageFiles(zip, manifest, staging, stagedFiles);
                prepareReplacements(installation, manifest);
                applyFiles(installation, stagedFiles, manifest.version());
                List<String> completed = runMigrations(zip, manifest, installation, installedVersion, staging);
                writeInstalledVersion(installation, manifest.version());
                return new UpdateResult(installedVersion, manifest.version(), completed);
            } finally {
                deleteTree(staging);
            }
        }
    }

    private UpdateManifest readManifest(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry("manifest.json");
        if (entry == null || entry.isDirectory()) {
            throw new IOException("Update package is missing manifest.json");
        }
        try (InputStream input = zip.getInputStream(entry)) {
            UpdateManifest manifest = GSON.fromJson(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8), UpdateManifest.class);
            if (manifest == null) throw new IOException("Invalid update manifest");
            return manifest;
        } catch (RuntimeException exception) {
            throw new IOException("Invalid update manifest", exception);
        }
    }

    private void validateManifest(UpdateManifest manifest, String installedVersion) throws IOException {
        validateVersions(manifest, installedVersion);
        validateFiles(manifest.files());
        validateMigrations(manifest.migrations());
        validateReplacementDirectories(manifest.replaceDirectories());
        validateRemovalPatterns(manifest.removePatterns());
    }

    private void validateVersions(UpdateManifest manifest, String installedVersion) throws IOException {
        if (!PRODUCT.equals(manifest.product())) throw new IOException("Unexpected update product: " + manifest.product());
        if (!validVersion(installedVersion) || !validVersion(manifest.version()) || !validVersion(manifest.minimumVersion())) {
            throw new IOException("Versions must use numeric major.minor[.patch] format");
        }
        if (compareVersions(installedVersion, manifest.minimumVersion()) < 0) {
            throw new IOException("Update requires at least version " + manifest.minimumVersion());
        }
        if (compareVersions(manifest.version(), installedVersion) <= 0) {
            throw new IOException("Target version must be newer than installed version");
        }
    }

    private void validateFiles(List<UpdateManifest.UpdateFile> files) throws IOException {
        if (files == null) throw new IOException("Manifest files must be present");
        Set<String> targets = new HashSet<>();
        for (UpdateManifest.UpdateFile file : files) {
            if (file == null || !isSha256(file.sha256())) throw new IOException("Each update file needs a SHA-256 checksum");
            safeZipEntry(file.source());
            UpdatePaths.resolveInside(Path.of("/installation-root"), file.target());
            if (file.executable() && !file.target().endsWith(".sh")) {
                throw new IOException("Only shell scripts may be marked executable: " + file.target());
            }
            if (!targets.add(file.target())) throw new IOException("Duplicate update target: " + file.target());
        }
    }

    private void validateMigrations(List<UpdateManifest.MigrationEntry> migrations) throws IOException {
        if (migrations == null) throw new IOException("Manifest migrations must be present");
        Set<String> migrationIds = new HashSet<>();
        for (UpdateManifest.MigrationEntry migration : migrations) {
            if (migration == null || migration.id() == null || migration.id().isBlank() || !isSha256(migration.sha256())) throw new IOException("Each migration needs an id and SHA-256 checksum");
            safeZipEntry(migration.artifact());
            if (!migrationIds.add(migration.id())) throw new IOException("Duplicate migration id: " + migration.id());
        }
    }

    private void validateReplacementDirectories(List<String> directories) throws IOException {
        if (directories == null) return;
        for (String directory : directories) {
            UpdatePaths.resolveInside(Path.of("/installation-root"), directory);
            if (directory.contains("\\")) throw new IOException("Invalid replacement directory: " + directory);
        }
    }

    private void validateRemovalPatterns(List<String> patterns) throws IOException {
        if (patterns == null) return;
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank() || pattern.contains("/") || pattern.contains("\\")) {
                throw new IOException("Removal patterns must match files directly under the installation root");
            }
            try { Path.of("probe").getFileSystem().getPathMatcher("glob:" + pattern); }
            catch (RuntimeException invalidPattern) { throw new IOException("Invalid removal pattern: " + pattern, invalidPattern); }
        }
    }

    private void stageFiles(ZipFile zip, UpdateManifest manifest, Path staging, List<StagedFile> staged) throws IOException {
        for (UpdateManifest.UpdateFile file : manifest.files()) {
            ZipEntry entry = zip.getEntry(file.source());
            if (entry == null || entry.isDirectory()) throw new IOException("Update file is missing: " + file.source());
            Path stagedPath = staging.resolve("files").resolve(file.target()).normalize();
            if (!stagedPath.startsWith(staging.resolve("files"))) throw new IOException("Invalid target path: " + file.target());
            Files.createDirectories(stagedPath.getParent());
            try (InputStream input = zip.getInputStream(entry)) {
                Files.copy(input, stagedPath, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!file.sha256().equalsIgnoreCase(sha256(stagedPath))) throw new IOException("Checksum mismatch for " + file.source());
            staged.add(new StagedFile(stagedPath, file.target(), file.executable()));
        }
    }

    private void applyFiles(Path installation, List<StagedFile> staged, String version) throws IOException {
        for (StagedFile file : staged) {
            Path target = UpdatePaths.resolveInside(installation, file.target());
            if (Files.exists(target) && Files.isDirectory(target)) throw new IOException("Cannot replace directory with file: " + file.target());
        }
        for (StagedFile file : staged) {
            Path target = UpdatePaths.resolveInside(installation, file.target());
            Files.createDirectories(target.getParent());
            Path backup = backupPath(installation, version, file.target());
            if (Files.exists(target)) {
                Files.createDirectories(backup.getParent());
                Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
            Files.copy(file.stagedPath(), target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            if (file.executable()) setExecutable(target);
        }
    }

    private static void setExecutable(Path file) throws IOException {
        try {
            Files.setPosixFilePermissions(file, Set.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE));
        } catch (UnsupportedOperationException _) {
            // Filesystems without POSIX permissions, such as Windows, have no execute bit to set.
        }
    }

    private void prepareReplacements(Path installation, UpdateManifest manifest) throws IOException {
        String version = manifest.version();
        replaceDirectories(installation, version, manifest.replaceDirectories());
        removeMatchingFiles(installation, version, manifest.removePatterns());
    }

    private void replaceDirectories(Path installation, String version, List<String> directories) throws IOException {
        if (directories == null) return;
        for (String relativeDirectory : directories) {
            Path directory = UpdatePaths.resolveInside(installation, relativeDirectory);
            if (!Files.exists(directory)) continue;
            if (!Files.isDirectory(directory)) throw new IOException("Replacement path is not a directory: " + relativeDirectory);
            Path backup = backupPath(installation, version, relativeDirectory);
            if (!Files.exists(backup)) copyTree(directory, backup);
            deleteTree(directory);
        }
    }

    private void removeMatchingFiles(Path installation, String version, List<String> patterns) throws IOException {
        if (patterns == null) return;
        for (String pattern : patterns) {
            var matcher = installation.getFileSystem().getPathMatcher("glob:" + pattern);
            try (var entries = Files.list(installation)) {
                for (Path entry : entries.filter(path -> matcher.matches(path.getFileName())).toList()) {
                    removeMatchingFile(installation, version, entry);
                }
            }
        }
    }

    private void removeMatchingFile(Path installation, String version, Path entry) throws IOException {
        if (Files.isDirectory(entry)) throw new IOException("Removal pattern matched a directory: " + entry.getFileName());
        Path backup = backupPath(installation, version, entry.getFileName().toString());
        Files.createDirectories(backup.getParent());
        if (!Files.exists(backup)) Files.copy(entry, backup, StandardCopyOption.COPY_ATTRIBUTES);
        Files.delete(entry);
    }

    private static void copyTree(Path source, Path destination) throws IOException {
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path target = destination.resolve(source.relativize(path));
                if (Files.isDirectory(path)) Files.createDirectories(target);
                else Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    private List<String> runMigrations(ZipFile zip, UpdateManifest manifest, Path installation,
            String installedVersion, Path staging) throws IOException {
        MigrationHistory history = MigrationHistory.load(installation);
        List<String> completedThisRun = new ArrayList<>();
        for (UpdateManifest.MigrationEntry entry : manifest.migrations()) {
            if (history.contains(entry.id())) continue;
            ZipEntry jarEntry = zip.getEntry(entry.artifact());
            if (jarEntry == null || jarEntry.isDirectory()) throw new IOException("Migration artifact is missing: " + entry.artifact());
            Path jar = staging.resolve("migrations").resolve(entry.id() + ".jar");
            Files.createDirectories(jar.getParent());
            try (InputStream input = zip.getInputStream(jarEntry)) { Files.copy(input, jar, StandardCopyOption.REPLACE_EXISTING); }
            if (!entry.sha256().equalsIgnoreCase(sha256(jar))) throw new IOException("Checksum mismatch for migration " + entry.id());
            try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, Migration.class.getClassLoader())) {
                Migration migration = ServiceLoader.load(Migration.class, loader).stream()
                        .map(ServiceLoader.Provider::get).filter(candidate -> entry.id().equals(candidate.id())).findFirst()
                        .orElseThrow(() -> new IOException("Migration provider not found for " + entry.id()));
                migration.execute(new MigrationContext(installation, installedVersion, manifest.version()));
            } catch (IOException exception) {
                throw exception;
            } catch (MigrationException exception) {
                throw new IOException("Migration failed: " + entry.id(), exception);
            }
            history.markCompleted(entry.id());
            completedThisRun.add(entry.id());
        }
        return List.copyOf(completedThisRun);
    }

    private void writeInstalledVersion(Path installation, String version) throws IOException {
        Path file = installation.resolve("data/system/cms-version");
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, version + System.lineSeparator());
        try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (IOException _) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
    }

    private Path backupPath(Path installation, String version, String target) {
        return UpdatePaths.resolveInside(installation, "data/update-backups/" + version + "/" + target);
    }

    private static void safeZipEntry(String name) throws IOException {
        if (name == null || name.isBlank() || name.startsWith("/") || name.contains("\\")) throw new IOException("Invalid package path: " + name);
        Path path = Path.of(name).normalize();
        if (path.isAbsolute() || path.startsWith("..")) throw new IOException("Invalid package path: " + name);
    }

    private static boolean validVersion(String value) { return value != null && value.matches("\\d+(?:\\.\\d+){1,2}"); }
    private static int compareVersions(String left, String right) {
        String[] a = left.split("\\.");
        String[] b = right.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            long av = i < a.length ? Long.parseLong(a[i]) : 0;
            long bv = i < b.length ? Long.parseLong(b[i]) : 0;
            int comparison = Long.compare(av, bv);
            if (comparison != 0) return comparison;
        }
        return 0;
    }
    private static boolean isSha256(String value) { return value != null && value.matches("(?i)[0-9a-f]{64}"); }
    private static String sha256(Path file) throws IOException {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file))); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        }
    }

    private record StagedFile(Path stagedPath, String target, boolean executable) { }
}
