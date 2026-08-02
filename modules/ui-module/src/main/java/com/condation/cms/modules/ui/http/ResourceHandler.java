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
import com.condation.cms.api.configuration.configs.ServerConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.feature.features.ConfigurationFeature;
import com.condation.cms.api.feature.features.HookSystemFeature;
import com.condation.cms.api.feature.features.ModuleManagerFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.modules.ui.extensionpoints.UILifecycleExtension;
import com.condation.cms.modules.ui.utils.ActionFactory;
import com.condation.cms.modules.ui.utils.TokenUtils;
import com.condation.cms.modules.ui.utils.TranslationHelper;
import com.condation.cms.modules.ui.utils.template.UILinkFunction;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class ResourceHandler extends JettyHandler {

	private final FileSystem fileSystem;
	private final String base;
	private final SiteModuleContext context;
	private final RequestContext requestContext;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		var hookSystem = requestContext.get(HookSystemFeature.class).hookSystem();
		var moduleManager = context.get(ModuleManagerFeature.class).moduleManager();
       	var siteProperties = context.get(ConfigurationFeature.class).configuration().get(SiteConfiguration.class).siteProperties();
		var actionFactory = new ActionFactory(context, siteProperties, hookSystem, moduleManager, getUser(request, context, requestContext).get());

		var resource = request.getHttpURI().getPath().replaceFirst(
				managerURL("/manager/", requestContext), "");

		if (resource.equals("")) {
			resource = "index.html";
		}

		if (resource.endsWith(".html")) {
			try {
				var secret = context.get(ConfigurationFeature.class).configuration().get(ServerConfiguration.class).serverProperties().secret();
				String content = UILifecycleExtension.getInstance(context).getTemplateEngine().render(resource,
						Map.of(
								"actionFactory", actionFactory,
								"csrfToken", TokenUtils.createToken("csrf", secret, Duration.ofHours(1), Duration.ofHours(1)),
								"links", new UILinkFunction(requestContext),
								"managerBaseURL", managerBaseURL(requestContext),
								"previewToken", TokenUtils.createToken(getUsername(request, context, requestContext), secret, Duration.ofHours(1), Duration.ofDays(7)),
								"contextPath", siteProperties.contextPath(),
								"siteId", siteProperties.id(),
								"translation", new TranslationHelper(siteProperties)
						));
				Content.Sink.write(response, true, content, callback);
			} catch (Exception e) {
				log.error("", e);
				callback.failed(e);
			}
		} else {
			var files = fileSystem.getPath(base);

			if (resource.startsWith("/")) {
				resource = resource.substring(1);
			}

			var path = files.resolve(resource);
			if (Files.exists(path)) {
				writeResource(path, response, callback);
			} else {
				path = files.resolve(resource + ".js");
				if (Files.exists(path)) {
					writeResource(path, response, callback);
				} else {
					callback.succeeded();
				}
			}
		}

		return true;
	}

	private void writeResource(Path path, Response response, Callback callback) throws Exception {
		String contentType = contentType(path);
		if (contentType.startsWith("text/")
				|| "application/json".equals(contentType)
				|| "image/svg+xml".equals(contentType)) {
			contentType += "; charset=UTF-8";
		}
		response.getHeaders().put(HttpHeader.CONTENT_TYPE, contentType);
		Content.Sink.write(response, true, ByteBuffer.wrap(Files.readAllBytes(path)));
		callback.succeeded();
	}

	private String contentType(Path path) throws Exception {
		String detected = Files.probeContentType(path);
		if (detected != null) {
			return detected;
		}

		String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
		if (fileName.endsWith(".js") || fileName.endsWith(".mjs")) {
			return "text/javascript";
		}
		if (fileName.endsWith(".css")) {
			return "text/css";
		}
		if (fileName.endsWith(".json")) {
			return "application/json";
		}
		if (fileName.endsWith(".svg")) {
			return "image/svg+xml";
		}
		if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
		if (fileName.endsWith(".webp")) {
			return "image/webp";
		}
		if (fileName.endsWith(".gif")) {
			return "image/gif";
		}
		return "application/octet-stream";
	}
}
