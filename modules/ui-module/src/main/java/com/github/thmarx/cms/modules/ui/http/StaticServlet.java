package com.github.thmarx.cms.modules.ui.http;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.github.thmarx.cms.modules.ui.utils.ServletUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("serial")
public class StaticServlet extends HttpServlet {
	
	private Path rootFolder;
	
	public StaticServlet (final Path rootFolder) {
		this.rootFolder = rootFolder;
	}
	
	public static interface LookupResult {
		public void respondGet(HttpServletResponse resp) throws IOException;
		public void respondHead(HttpServletResponse resp);
		public long getLastModified();
	}
	
	public static class Error implements LookupResult {
		protected final int statusCode;
		protected final String message;

		public Error(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}

		public long getLastModified() {
			return -1;
		}

		public void respondGet(HttpServletResponse resp) throws IOException {
			resp.sendError(statusCode,message);
		}

		public void respondHead(HttpServletResponse resp) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class StaticFile implements LookupResult {
		protected final long lastModified;
		protected final String mimeType;
		protected final long contentLength;
		protected final boolean acceptsDeflate;
		protected final URL url;

		public StaticFile(long lastModified, String mimeType, long contentLength, boolean acceptsDeflate, URL url) {
			this.lastModified = lastModified;
			this.mimeType = mimeType;
			this.contentLength = contentLength;
			this.acceptsDeflate = acceptsDeflate;
			this.url = url;
		}

		public long getLastModified() {
			return lastModified;
		}

		protected boolean willDeflate() {
			return acceptsDeflate && deflatable(mimeType) && contentLength >= deflateThreshold;
		}

		protected void setHeaders(HttpServletResponse resp) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(mimeType);
			if(contentLength >= 0 && !willDeflate())
				resp.setContentLengthLong(contentLength);
		}

		public void respondGet(HttpServletResponse resp) throws IOException {
			setHeaders(resp);
			final OutputStream os;
			if(willDeflate()) {
				resp.setHeader("Content-Encoding", "gzip");
				os = new GZIPOutputStream(resp.getOutputStream(), bufferSize);
			} else {
				os = resp.getOutputStream();
			}
			
			transferStreams(url.openStream(),os);
		}

		public void respondHead(HttpServletResponse resp) {
			if(willDeflate())
				throw new UnsupportedOperationException();
			setHeaders(resp);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		lookup(req).respondGet(resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doGet(req, resp);
	}
	
	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		try {
			lookup(req).respondHead(resp);
		} catch(UnsupportedOperationException e) {
			super.doHead(req, resp);
		}
	}
	
	@Override
	protected long getLastModified(HttpServletRequest req) {
		return lookup(req).getLastModified();
	}
	
	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult)req.getAttribute("lookupResult");
		if(r == null) {
			r = lookupNoCache(req);
			req.setAttribute("lookupResult", r);
		}
		return r;
	}
	
	protected LookupResult lookupNoCache(HttpServletRequest req) {
		
		var uri = req.getRequestURI();
		uri = uri.replace(req.getContextPath(), "");
		uri = uri.replace(req.getServletPath(), "");
		
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		
		Path staticPath = rootFolder.resolve(uri);
		if (!Files.exists(staticPath)) {
			return new Error(HttpServletResponse.SC_NOT_FOUND, "not found");
		}
		final String mimeType = getMimeType(staticPath.getFileName().toString());
		
		try {
		return new StaticFile(
				Files.getLastModifiedTime(staticPath).toMillis(),
				mimeType,
				Files.size(staticPath),
				acceptsDeflate(req),
				staticPath.toUri().toURL());
		
		} catch(Exception e) {
			return new Error(HttpServletResponse.SC_BAD_REQUEST, "Malformed path");
		}
	}
	
	protected String getPath(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		String pathInfo = ServletUtils.coalesce(req.getPathInfo(), "");
		return servletPath + pathInfo;
	}
	
	protected boolean isForbidden(String path) {
		String lpath = path.toLowerCase();
		return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
	}
		
	protected String getMimeType(String path) {
		return ServletUtils.coalesce(getServletContext().getMimeType(path),"application/octet-stream");
	}
	
	protected static boolean acceptsDeflate(HttpServletRequest req) {
		final String ae = req.getHeader("Accept-Encoding");
		return ae != null && ae.contains("gzip");
	}
	
	protected static boolean deflatable(String mimetype) {
		return mimetype.startsWith("text/")
			|| mimetype.equals("application/postscript")
			|| mimetype.startsWith("application/ms")
			|| mimetype.startsWith("application/vnd")
			|| mimetype.endsWith("xml");
	}
	
	protected static final int deflateThreshold = 4*1024;
	
	protected static final int bufferSize = 4*1024;
	
	protected static void transferStreams(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] buf = new byte[bufferSize];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1)
				os.write(buf, 0, bytesRead);
		} finally {
			is.close();
			os.close();
		}
	}
}
