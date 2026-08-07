package com.condation.cms.modules.ui.http;

/*-
 * #%L
 * UI Module
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
import com.condation.cms.api.utils.SecureFileUtils;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import com.condation.cms.modules.ui.utils.json.UIGsonProvider;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
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

	private final Path tempUploadDir;

	private final boolean useDateFolder;

	/**
	 * Maximum allowed size of uploaded files in bytes (10 MB).
	 */
	public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

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

	public UploadHandler(String contextPath, Path outputDir, boolean useDateFolder) throws IOException {
		this(contextPath, prepareOutputDirectory(outputDir), useDateFolder);
	}

	private UploadHandler(String contextPath, OutputDirectory outputDirectory, boolean useDateFolder) throws IOException {
		super();

		this.useDateFolder = useDateFolder;
		this.contextPath = contextPath;
		this.outputDir = outputDirectory.path();
		this.tempUploadDir = SecureFileUtils.ensurePrivateDirectory(
				outputDirectory.parent().resolve(".condation-upload-work"));
	}

	private static OutputDirectory prepareOutputDirectory(Path outputDir) throws IOException {
		Path normalizedOutputDir = outputDir.toAbsolutePath().normalize();
		ensureDirExists(normalizedOutputDir);

		Path realPath = normalizedOutputDir.toRealPath();
		Path parent = realPath.getParent();

		if (parent == null) {
			throw new IOException("Upload directory must have a parent: " + outputDir);
		}

		return new OutputDirectory(realPath, parent);
	}

	private record OutputDirectory(Path path, Path parent) {

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
		formData.setFilesDirectory(tempUploadDir);

		try {
			formData.parse(request, new org.eclipse.jetty.util.Promise.Invocable<MultiPartFormData.Parts>() {
				@Override
				public void failed(Throwable x) {
					Response.writeError(request, response, callback, x);
				}

				@Override
				public void succeeded(MultiPartFormData.Parts parts) {
					if (parts == null || parts.size() == 0) {
						log.warn("Multipart upload received, but no parts found.");
						Response.writeError(request, response, callback, HttpStatus.BAD_REQUEST_400, "No parts in upload.");
						return;
					}

					try {
						var filename = process(parts);
						response.setStatus(HttpStatus.OK_200);
						Content.Sink.write(response, true, UIGsonProvider.INSTANCE.toJson(Map.of("filename", filename)), callback);
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

	private String process(MultiPartFormData.Parts parts) throws IOException {
		try {
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

			if (useDateFolder) {
				LocalDate now = LocalDate.now();
				String year = String.valueOf(now.getYear());
				String month = String.format("%02d", now.getMonthValue());

				uri = "%s/%s/".formatted(year, month);
			}

			if (filePart != null) {
				String rawFilename = filePart.getFileName();
				if (StringUtil.isNotBlank(rawFilename)) {
					// Temporäre Datei erzeugen, um MIME-Type zu ermitteln
					Path tempFile = SecureFileUtils.createPrivateTempFile(
							tempUploadDir, "upload-", ".tmp");
					try {
						long bytesCopied;
						try (InputStream inputStream = ByteStreams.limit(
								Content.Source.asInputStream(filePart.getContentSource()),
								MAX_FILE_SIZE_BYTES + 1); OutputStream outputStream = Files.newOutputStream(tempFile)) {
							bytesCopied = ByteStreams.copy(inputStream, outputStream);
						}

						if (bytesCopied > MAX_FILE_SIZE_BYTES) {
							throw new IOException("Uploaded file too large (more than "
									+ MAX_FILE_SIZE_BYTES + " bytes)");
						}

						String detectedMimeType = tika.detect(tempFile);
						log.debug("Detected MIME type: {}", detectedMimeType);

						if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
							throw new IOException("Unsupported file type: " + detectedMimeType);
						}

						String safeFilename = slugifyFilename(rawFilename);
						Path targetDir = outputDir;

						if (StringUtil.isNotBlank(uri)) {
							uri = uri.replaceAll("[^a-zA-Z0-9/_\\-]", "_");
							targetDir = outputDir.resolve(uri).normalize();
						}

						// Check before creating directories, then resolve symlinks and check again.
						if (!targetDir.startsWith(outputDir)) {
							throw new IOException("Upload target escapes the configured output directory");
						}
						ensureDirExists(targetDir);
						Path realTargetDir = targetDir.toRealPath();
						if (!realTargetDir.startsWith(outputDir)) {
							throw new IOException("Upload target resolves outside the configured output directory");
						}

						Path outputFile = realTargetDir.resolve(safeFilename).normalize();
						if (!realTargetDir.equals(outputFile.getParent())) {
							throw new IOException("Invalid upload filename");
						}

						Files.move(
								tempFile,
								outputFile,
								java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						log.info("Saved uploaded file to {}", outputFile);

						return PathUtil.toRelativeFile(outputFile, outputDir);
					} finally {
						Files.deleteIfExists(tempFile);
					}
				}
			}
		} finally {
			for (MultiPart.Part part : parts) {
				part.delete();
			}
		}

		return "";
	}

	String slugifyFilename(String rawFilename) throws IOException {
		String normalizedFilename = rawFilename.replace('\\', '/');
		int slashIndex = normalizedFilename.lastIndexOf('/');
		if (slashIndex >= 0) {
			normalizedFilename = normalizedFilename.substring(slashIndex + 1);
		}
		if (normalizedFilename.isBlank()) {
			throw new IOException("Invalid upload filename");
		}

		String extension = "";
		int dotIndex = normalizedFilename.lastIndexOf('.');
		String namePart = normalizedFilename;

		if (dotIndex > 0 && dotIndex < normalizedFilename.length() - 1) {
			extension = normalizedFilename.substring(dotIndex);
			if (!extension.matches("\\.[a-zA-Z0-9]{1,10}")) {
				throw new IOException("Invalid upload file extension");
			}
			namePart = normalizedFilename.substring(0, dotIndex);
		}

		// Slugify nur auf den Namensteil anwenden
		String slug = UIPathUtil.slugify(namePart);
		if (slug.isBlank()) {
			throw new IOException("Invalid upload filename");
		}

		// Endung wieder anhängen
		return slug + extension.toLowerCase();
	}

}
