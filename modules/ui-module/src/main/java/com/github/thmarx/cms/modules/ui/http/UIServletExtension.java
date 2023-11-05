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
import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.Mapping;
import com.github.thmarx.cms.api.extensions.ServletExtensionPoint;
import com.github.thmarx.cms.api.extensions.ServletMapping;
import com.github.thmarx.modules.api.annotation.Extension;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 *
 * @author t.marx
 */
@Extension(ServletExtensionPoint.class)
@Slf4j
public class UIServletExtension extends ServletExtensionPoint {

	FileSystem fileSystem;
	
	@Override
	public void init() {
		try {
			URL resource = UIServletExtension.class.getResource("/files");
			
			fileSystem = FileSystems.getFileSystem(resource.toURI());
		} catch (URISyntaxException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	public static FileSystem createFileSystem () {
		try {
			URL resource = UIServletExtension.class.getResource("/files");
			
			return FileSystems.getFileSystem(resource.toURI());
		} catch (URISyntaxException ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}
	
	
	@Override
	public ServletMapping getMapping() {
		ServletMapping mapping = new ServletMapping();

		try {

			mapping.add("/assets/*", new AssetsServlet());
			mapping.add("/static/*", new StaticServlet(createFileSystem().getPath("/files")));

		} catch (Exception ex) {
			log.error(null, ex);
		}
		return mapping;
	}

	public static class AssetsServlet extends HttpServlet {

		@Override
		protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
			var uri = request.getRequestURI();
			uri = uri.replace(request.getContextPath(), "");
			uri = uri.replace(request.getServletPath(), "");
			
			System.out.println(uri);
//			System.out.println(UIServletExtension.this.fileSystem.getPath(uri));
			Path assetPath = createFileSystem().getPath(uri);
			System.out.println(assetPath);
			System.out.println(Files.exists(assetPath));
		}
		
		
	}
	
}
