package com.github.thmarx.cms.modules.example;

/*-
 * #%L
 * example-module
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

import com.github.thmarx.cms.api.CMSModuleContext;
import com.github.thmarx.cms.api.extensions.JettyHttpHandlerExtensionPoint;
import com.github.thmarx.cms.api.extensions.Mapping;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(JettyHttpHandlerExtensionPoint.class)
public class SitemapJettyHttpHandlerExtension extends JettyHttpHandlerExtensionPoint {

	@Override
	public Mapping getMapping() {
		Mapping mapping = new Mapping();
		mapping.add(PathSpec.from("/sitemap.xml"), new SitemapHandler(getContext()));
		return mapping;
	}
	
	@RequiredArgsConstructor
	public static class SitemapHandler extends Handler.Abstract {

		private final CMSModuleContext context;
		
		@Override
		public boolean handle(Request request, Response response, Callback callback) throws Exception {
			
			try (var sitemap = new SitemapGenerator(
					Response.asBufferedOutputStream(request, response),
					context.getSiteProperties()
			)) {
				response.getHeaders().add(HttpHeader.CONTENT_TYPE, "application/xml");
				sitemap.start();
				context.getDb().getContent().query((node, length) -> node).get().forEach(node -> {
					try {
						sitemap.addNode(node);
					} catch (IOException ex) {
						log.error(null, ex);
					}
				});
			} catch (Exception e) {
				log.error(null, e);
			}
			callback.succeeded();
			
			return true;
		}
		
	}
}
