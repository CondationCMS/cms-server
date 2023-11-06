package com.github.thmarx.cms.modules.ui.extensionpoints;

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
import com.github.thmarx.cms.modules.ui.http.FileSystemCreateHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemDeleteHandler;
import com.github.thmarx.cms.modules.ui.http.FileSystemListHandler;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
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
			return FileSystems.newFileSystem(URI.create(array[0]), env);
			
		} catch (URISyntaxException | IOException ex) {
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
			

		} catch (Exception ex) {
			log.error(null, ex);
		}
		return mapping;
	}

}
