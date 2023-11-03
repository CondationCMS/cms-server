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
import com.github.thmarx.modules.api.annotation.Extension;
import org.eclipse.jetty.http.pathmap.PathSpec;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.ResourceFactory;

/**
 *
 * @author t.marx
 */
@Extension(JettyHttpHandlerExtensionPoint.class)
public class UIJettyHttpHandlerExtension extends JettyHttpHandlerExtensionPoint {

	@Override
	public String getContextPath() {
		return "ui";
	}

	@Override
	public Mapping getHandler() {

		var classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(UIJettyHttpHandlerExtension.class.getClassLoader());

			ResourceHandler resourceHandler = new ResourceHandler();
			resourceHandler.setBaseResource(
					ResourceFactory.of(resourceHandler)
							.newClassLoaderResource("com/github/thmarx/cms/modules/ui/assets/", true)
			);

			Mapping mapping = new Mapping();
			mapping.add(PathSpec.from("/assets/*"), resourceHandler);

			return mapping;

		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

}
