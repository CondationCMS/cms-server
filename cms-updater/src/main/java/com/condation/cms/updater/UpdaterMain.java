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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Command-line entry point for the executable updater artifact. */
public final class UpdaterMain {
    private UpdaterMain() { }

    public static void main(String[] args) {
        try {
            if (args.length > 0 && "create-package".equals(args[0])) {
                createPackage(args);
                return;
            }
            run(args);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            UpdaterConsole.error("Update interrupted");
            System.exit(1);
        } catch (IOException | IllegalArgumentException exception) {
            UpdaterConsole.error("Update failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    static UpdateResult run(String[] args) throws IOException, InterruptedException {
        Options options = parse(args);
        Path installation = options.home().toAbsolutePath().normalize();
        if (!Files.isDirectory(installation)) throw new IllegalArgumentException("Installation home not found: " + installation);
        new ServerProcessChecker().ensureStopped(installation);
        String currentVersion = new InstalledVersionResolver().resolve(installation);
        UpdateResult result;
        try (UpdatePackageSource source = UpdatePackageSource.open(options.packageLocation())) {
            if (!Files.isRegularFile(source.path())) throw new IllegalArgumentException("Update package not found: " + source.path());
            result = new UpdateInstaller().install(installation, source.path(), currentVersion);
        }
        UpdaterConsole.info("Updated CondationCMS from " + result.previousVersion() + " to " + result.installedVersion());
        for (String migration : result.migrationsExecuted()) UpdaterConsole.info("Migration completed: " + migration);
        return result;
    }

    static Options parse(String[] args) {
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            throw new IllegalArgumentException(usage());
        }
        if (args.length == 0 || !"install".equals(args[0])) throw new IllegalArgumentException(usage());
        Path home = Path.of("../");
        String packageLocation = null;
        int i = 1;
        while (i < args.length) {
            String option = args[i];
            if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + option + "\n" + usage());
            String value = args[i + 1];
            i += 2;
            switch (option) {
                case "--home" -> home = Path.of(value);
                case "--package" -> packageLocation = value;
                default -> throw new IllegalArgumentException("Unknown option: " + option + "\n" + usage());
            }
        }
        if (packageLocation == null || packageLocation.isBlank()) throw new IllegalArgumentException("--package is required\n" + usage());
        return new Options(home, packageLocation);
    }

    private static String usage() {
        return """
                Usage:
                  java -jar cms-updater.jar install [--home <installation>] --package <update.zip|URL>
                  java -jar cms-updater.jar create-package --home <release-home> --scripts <scripts-dir> --output <update.zip> --version <version> --minimum-version <version>
                Default install home: ../ (relative to the current working directory)""";
    }

    private static void createPackage(String[] args) throws IOException {
        var options = parsePackageBuild(args);
        Path output = new UpdatePackageBuilder().build(options.home(), options.scripts(), options.output(),
                options.version(), options.minimumVersion());
        UpdaterConsole.info("Created update package: " + output);
    }

    private static PackageBuildOptions parsePackageBuild(String[] args) {
        Path home = null;
        Path scripts = null;
        Path output = null;
        String version = null;
        String minimumVersion = null;
        int i = 1;
        while (i < args.length) {
            String option = args[i];
            if (i + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + option + "\n" + usage());
            String value = args[i + 1];
            i += 2;
            switch (option) {
                case "--home" -> home = Path.of(value);
                case "--scripts" -> scripts = Path.of(value);
                case "--output" -> output = Path.of(value);
                case "--version" -> version = value;
                case "--minimum-version" -> minimumVersion = value;
                default -> throw new IllegalArgumentException("Unknown option: " + args[i - 1] + "\n" + usage());
            }
        }
        if (home == null || scripts == null || output == null || version == null || minimumVersion == null) {
            throw new IllegalArgumentException("Missing create-package option\n" + usage());
        }
        return new PackageBuildOptions(home, scripts, output, version, minimumVersion);
    }

    record Options(Path home, String packageLocation) { }
    private record PackageBuildOptions(Path home, Path scripts, Path output, String version, String minimumVersion) { }
}
