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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Resolves a local update package or downloads an HTTP(S) package to a temporary file. */
final class UpdatePackageSource implements AutoCloseable {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final Path path;
    private final boolean temporary;

    private UpdatePackageSource(Path path, boolean temporary) {
        this.path = path;
        this.temporary = temporary;
    }

    static UpdatePackageSource open(String value) throws IOException, InterruptedException {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            URI uri;
            try { uri = URI.create(value); }
            catch (IllegalArgumentException exception) { throw new IOException("Invalid update package URL", exception); }
            Path temporaryFile = UpdaterTempFiles.createFile("condation-update-download-", ".zip");
            boolean success = false;
            try {
                HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(5)).GET().build();
                HttpResponse<Path> response = HTTP.send(request, HttpResponse.BodyHandlers.ofFile(temporaryFile));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("Could not download update package: HTTP " + response.statusCode());
                }
                success = true;
                return new UpdatePackageSource(temporaryFile, true);
            } finally {
                if (!success) Files.deleteIfExists(temporaryFile);
            }
        }
        if (value.startsWith("file:")) {
            try { return new UpdatePackageSource(Path.of(URI.create(value)), false); }
            catch (IllegalArgumentException exception) { throw new IOException("Invalid update package file URL", exception); }
        }
        return new UpdatePackageSource(Path.of(value).toAbsolutePath().normalize(), false);
    }

    Path path() { return path; }

    @Override
    public void close() throws IOException {
        if (temporary) Files.deleteIfExists(path);
    }
}
