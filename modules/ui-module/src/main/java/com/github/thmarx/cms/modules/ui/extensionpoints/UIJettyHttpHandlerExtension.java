package com.github.thmarx.cms.modules.ui.extensionpoints;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.Mapping;
import com.github.thmarx.cms.modules.ui.http.FileSystemCreateHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemDeleteHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemListHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemReadHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemWriteHandler;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.pathmap.PathSpec;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 *
 * @author t.marx
 */
@Extension(JettyHttpHandlerExtensionPoint.class)
@Slf4j
public class UIJettyHttpHandlerExtension extends JettyHttpHandlerExtensionPoint {

	public static FileSystem createFileSystem () {
		try {
			URL resource = UIJettyHttpHandlerExtension.class.getResource("/files");
			
			final Map<String, String> env = new HashMap<>();
			final String[] array = resource.toURI().toString().split("!");
			try {
				return FileSystems.getFileSystem(URI.create(array[0]));
			} catch (FileSystemNotFoundException fsnfe) {
				
			}
			
			return FileSystems.newFileSystem(URI.create(array[0]), env);
			
		} catch (URISyntaxException | IOException  ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public Mapping getMapping() {
		Mapping mapping = new Mapping();

		try {
			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setDirAllowed(false);
			resourceHandler.setBaseResource(ResourceFactory.of(resourceHandler)
					.newResource(createFileSystem().getPath("/files")));
			
			
			mapping.add(PathSpec.from("/assets/*"), resourceHandler);
			mapping.add(PathSpec.from("/file-system/list"), new FileSystemListHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/create"), new FileSystemCreateHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/delete"), new FileSystemDeleteHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/read"), new FileSystemReadHandler(UILifecycleExtension.fileSystemService));
			mapping.add(PathSpec.from("/file-system/write"), new FileSystemWriteHandler(UILifecycleExtension.fileSystemService));
			

		} catch (Exception ex) {
			log.error(null, ex);
		}
		return mapping;
	}

}
