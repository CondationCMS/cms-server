package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.IO;
import static org.eclipse.jetty.util.IO.ensureDirExists;
import org.eclipse.jetty.util.StringUtil;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
public class UploadHandler extends JettyHandler {

	private final String contextPath;
	private final Path outputDir;

	private final Path TEMP_UPLOAD_DIR;

	public static final Set<String> ALLOWED_MIME_TYPES = Set.of(
			"image/png",
			"image/jpeg",
			"image/gif",
			"image/webp",
			"image/svg+xml",
			"image/tiff",
			"image/avif"
	);

	private static final Tika tika = new Tika();

	public UploadHandler(String contextPath, Path outputDir) throws IOException {
		super();
		this.contextPath = contextPath;
		this.outputDir = outputDir;
		ensureDirExists(this.outputDir);
		this.TEMP_UPLOAD_DIR = Files.createTempDirectory("condation-uploads");
	}

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		if (!request.getHttpURI().getPath().startsWith(contextPath)) {
			// not meant for us, skip it.
			return false;
		}

		if (!request.getMethod().equalsIgnoreCase("POST")) {
			// Not a POST method
			Response.writeError(request, response, callback, HttpStatus.METHOD_NOT_ALLOWED_405);
			return true;
		}

		String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
		if (!HttpField.getValueParameters(contentType, null).equals("multipart/form-data")) {
			// Not a content-type supporting multi-part
			Response.writeError(request, response, callback, HttpStatus.NOT_ACCEPTABLE_406);
			return true;
		}

		String boundary = MultiPart.extractBoundary(contentType);
		MultiPartFormData.Parser formData = new MultiPartFormData.Parser(boundary);
		formData.setFilesDirectory(TEMP_UPLOAD_DIR);

		try {
			formData.parse(request, new org.eclipse.jetty.util.Promise.Invocable<MultiPartFormData.Parts>() {
				@Override
				public void accept(MultiPartFormData.Parts parts, Throwable u) {
					if (u != null) {
						Response.writeError(request, response, callback, u);
						return;
					}

					if (parts == null || parts.size() == 0) {
						log.warn("Multipart upload received, but no parts found.");
						Response.writeError(request, response, callback, HttpStatus.BAD_REQUEST_400, "No parts in upload.");
						return;
					}

					try {
						process(parts);
						response.setStatus(HttpStatus.OK_200);
						callback.succeeded();
					} catch (Exception ex) {
						log.error("Fehler beim Verarbeiten des Uploads", ex);
						Response.writeError(request, response, callback, ex);
					}
				}
			});
		} catch (Exception x) {
			Response.writeError(request, response, callback, x);
		}
		return true;
	}

	private void process(MultiPartFormData.Parts parts) throws IOException {
		MultiPart.Part filePart = null;
		String uri = null;

		for (MultiPart.Part part : parts) {
			if ("uri".equals(part.getName())) {
				try (InputStream is = Content.Source.asInputStream(part.getContentSource())) {
					uri = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
				}
			} else if ("file".equals(part.getName())) {
				filePart = part;
			}
		}

		if (filePart != null) {
			String rawFilename = filePart.getFileName();
			if (StringUtil.isNotBlank(rawFilename)) {
				// Temporäre Datei erzeugen, um MIME-Type zu ermitteln
				Path tempFile = Files.createTempFile("upload-", ".tmp");
				try (InputStream inputStream = Content.Source.asInputStream(filePart.getContentSource()); OutputStream outputStream = Files.newOutputStream(tempFile)) {
					IO.copy(inputStream, outputStream);
				}

				String detectedMimeType = tika.detect(tempFile);
				log.debug("Detected MIME type: {}", detectedMimeType);

				if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
					Files.deleteIfExists(tempFile);
					throw new IOException("Unsupported file type: " + detectedMimeType);
				}

				// Zieldatei vorbereiten
				String safeFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8);
				Path targetDir = outputDir;

				if (StringUtil.isNotBlank(uri)) {
					uri = uri.replaceAll("[^a-zA-Z0-9/_\\-]", "_"); // nur sichere Zeichen
					targetDir = outputDir.resolve(uri).normalize();
				}

				ensureDirExists(targetDir);
				Path outputFile = targetDir.resolve(safeFilename);

				// Temporäre Datei an Zielort verschieben
				Files.move(tempFile, outputFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				log.info("Saved uploaded file to {}", outputFile);
			}
		}

		for (MultiPart.Part part : parts) {
			part.delete();
		}
	}

}
