/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.modules.ui.http;

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
public class UploadHandler extends Handler.Abstract {

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
			String responseBody = process(formData.parse(request).join()); // May block waiting for multipart form data.
			response.setStatus(HttpStatus.OK_200);
			response.write(true, BufferUtil.toBuffer(responseBody), callback);
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
