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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MultiPart;
import org.eclipse.jetty.http.MultiPartFormData;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.BufferUtil;
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

	public UploadHandler(String contextPath, Path outputDir) throws IOException {
		super();
		this.contextPath = contextPath;
		this.outputDir = outputDir.resolve("handler");
		ensureDirExists(this.outputDir);
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
		formData.setFilesDirectory(outputDir);

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
					} catch (IOException ex) {
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
		StringWriter body = new StringWriter();
		PrintWriter out = new PrintWriter(body);

		for (MultiPart.Part part : parts) {
			out.printf("Got Part[%s].length=%s%n", part.getName(), part.getLength());
			HttpFields headers = part.getHeaders();
			for (HttpField field : headers) {
				out.printf("Got Part[%s].header[%s]=%s%n", part.getName(), field.getName(), field.getValue());
			}
			out.printf("Got Part[%s].fileName=%s%n", part.getName(), part.getFileName());
			String filename = part.getFileName();
			if (StringUtil.isNotBlank(filename)) {
				// ensure we don't have "/" and ".." in the raw form.
				filename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

				Path outputFile = outputDir.resolve(filename);
				try (InputStream inputStream = Content.Source.asInputStream(part.getContentSource()); OutputStream outputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					IO.copy(inputStream, outputStream);
					out.printf("Saved Part[%s] to %s%n", part.getName(), outputFile);
				}
			}
		}

		return body.toString();
	}
}
