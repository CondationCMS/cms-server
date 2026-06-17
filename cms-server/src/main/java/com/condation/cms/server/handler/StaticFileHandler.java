package com.condation.cms.server.handler;

/*-
 * #%L
 * CMS Server
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

import com.condation.cms.api.utils.PathUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

@Slf4j
public class StaticFileHandler extends AbstractHandler {

    private static final DateTimeFormatter HTTP_DATE_FORMATTER
            = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    private static final String CACHE_CONTROL
            = "public, max-age=0, must-revalidate";

    private final List<Path> bases;

    public StaticFileHandler(List<Path> bases) {
        this.bases = List.copyOf(bases);
    }

    @Override
    public boolean handle(
            Request request,
            Response response,
            Callback callback
    ) throws Exception {

        if (!isSupportedMethod(request.getMethod())) {
            return false;
        }

        String relativePath = getRelativePath(request);

        for (Path base : bases) {
            Path normalizedBase = base.toAbsolutePath().normalize();
            Path requested = normalizedBase.resolve(relativePath).normalize();

            // Günstige lexikalische Prüfung vor Dateisystemzugriffen.
            if (!PathUtil.isChild(normalizedBase, requested)) {
                continue;
            }

            if (!Files.isRegularFile(requested)) {
                continue;
            }

            Path realBase;
            Path realRequested;

            try {
                realBase = normalizedBase.toRealPath();
                realRequested = requested.toRealPath();
            } catch (IOException e) {
                continue;
            }

            // Verhindert das Verlassen des Basisverzeichnisses über Symlinks.
            if (!realRequested.startsWith(realBase)) {
                log.warn(
                        "Rejected static resource outside base path: {}",
                        realRequested
                );
                continue;
            }

            return serveFile(request, response, callback, realRequested);
        }

        return false;
    }

    private boolean isSupportedMethod(String method) {
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method);
    }

    private boolean serveFile(
            Request request,
            Response response,
            Callback callback,
            Path requested
    ) {
        try {
            long size = Files.size(requested);
            Instant lastModified = truncateToHttpDatePrecision(
                    Files.getLastModifiedTime(requested)
            );
            String etag = buildEtag(size, lastModified);

            response.getHeaders().put(HttpHeader.ETAG, etag);
            response.getHeaders().put(
                    HttpHeader.LAST_MODIFIED,
                    HTTP_DATE_FORMATTER.format(lastModified)
            );
            response.getHeaders().put(
                    HttpHeader.CACHE_CONTROL,
                    CACHE_CONTROL
            );

            if (isNotModified(request, etag, lastModified)) {
                response.setStatus(HttpStatus.NOT_MODIFIED_304);
                callback.succeeded();
                return true;
            }

            response.setStatus(HttpStatus.OK_200);
            response.getHeaders().put(
                    HttpHeader.CONTENT_TYPE,
                    guessContentType(requested)
            );
            response.getHeaders().put(
                    HttpHeader.CONTENT_LENGTH,
                    Long.toString(size)
            );

            if ("HEAD".equalsIgnoreCase(request.getMethod())) {
                callback.succeeded();
                return true;
            }

            InputStream input = Files.newInputStream(requested);

            Content.copy(
                    Content.Source.from(input),
                    response,
                    new Callback() {
                @Override
                public void succeeded() {
                    closeQuietly(input);
                    callback.succeeded();
                }

                @Override
                public void failed(Throwable failure) {
                    closeQuietly(input);
                    callback.failed(failure);
                }
            }
            );

            return true;

        } catch (IOException e) {
            log.error("Error serving static file {}", requested, e);
            callback.failed(e);
            return true;
        }
    }

    private Instant truncateToHttpDatePrecision(FileTime fileTime) {
        return fileTime.toInstant().truncatedTo(ChronoUnit.SECONDS);
    }

    private String buildEtag(long size, Instant lastModified) {
        return "W/\"%x-%x\"".formatted(
                size,
                lastModified.getEpochSecond()
        );
    }

    private boolean isNotModified(
            Request request,
            String etag,
            Instant lastModified
    ) {
        String ifNoneMatch = request.getHeaders().get(
                HttpHeader.IF_NONE_MATCH
        );

        if (ifNoneMatch != null) {
            return matchesEtag(ifNoneMatch, etag);
        }

        String ifModifiedSince = request.getHeaders().get(
                HttpHeader.IF_MODIFIED_SINCE
        );

        if (ifModifiedSince == null) {
            return false;
        }

        try {
            Instant clientLastModified = Instant.from(
                    HTTP_DATE_FORMATTER.parse(ifModifiedSince)
            );

            return !lastModified.isAfter(clientLastModified);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean matchesEtag(String ifNoneMatch, String etag) {
        if ("*".equals(ifNoneMatch.trim())) {
            return true;
        }

        String normalizedEtag = normalizeEtag(etag);

        for (String candidate : ifNoneMatch.split(",")) {
            if (normalizedEtag.equals(normalizeEtag(candidate))) {
                return true;
            }
        }

        return false;
    }

    private String normalizeEtag(String etag) {
        String normalized = etag.trim();

        if (normalized.startsWith("W/")) {
            normalized = normalized.substring(2);
        }

        return normalized;
    }

    private String guessContentType(Path path) {
        try {
            String type = Files.probeContentType(path);

            if (type != null) {
                return type;
            }
        } catch (IOException e) {
            log.debug("Could not detect content type for {}", path, e);
        }

        String filename = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (filename.endsWith(".js") || filename.endsWith(".mjs")) {
            return "text/javascript";
        }
        if (filename.endsWith(".css")) {
            return "text/css";
        }
        if (filename.endsWith(".html")
                || filename.endsWith(".htm")) {
            return "text/html";
        }
        if (filename.endsWith(".json")) {
            return "application/json";
        }
        if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (filename.endsWith(".webmanifest")) {
            return "application/manifest+json";
        }
        if (filename.endsWith(".wasm")) {
            return "application/wasm";
        }

        return "application/octet-stream";
    }

    private void closeQuietly(InputStream input) {
        try {
            input.close();
        } catch (IOException e) {
            log.debug("Could not close static file stream", e);
        }
    }
}
