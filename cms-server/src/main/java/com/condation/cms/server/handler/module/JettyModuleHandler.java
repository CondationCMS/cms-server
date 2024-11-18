package com.condation.cms.server.handler.module;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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


import com.condation.cms.api.extensions.HttpHandlerExtensionPoint;
import com.condation.cms.api.extensions.Mapping;
import com.condation.modules.api.ModuleManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class JettyModuleHandler extends Handler.Abstract {

	public static final String PATH = "module";
	
	private final ModuleManager moduleManager;
	private final List<String> activeModules;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		try {
			String moduleId = getModuleID(request);

			if (!activeModules.contains(moduleId)) {
				Response.writeError(request, response, callback, 404);
				return false;
			}

			var uri = getModuleUri(request);

			Optional<Mapping> firstMatch = moduleManager.module(moduleId).extensions(HttpHandlerExtensionPoint.class)
					.stream()
					.filter(extension -> extension.getMapping().getMatchingHandler(uri).isPresent())
					.map(extension -> extension.getMapping())
					.findFirst();

			if (firstMatch.isPresent()) {
				var mapping = firstMatch.get();
				var handler = mapping.getMatchingHandler(uri).get();
				return handler.handle(request, response, callback);
			}

			Response.writeError(request, response, callback, 404);
			return true;
		} catch (Exception e) {
			log.error(null, e);
			callback.failed(e);
			return true;
		}

	}

	private String getModuleUri(Request request) {
		var modulePath = getModulePath(request);
		
		var moduleID = getModuleID(request);
		
		return modulePath.replace(PATH + "/" + moduleID, "");
	}

	private String getModuleID(Request request) {
		var modulePath = getModulePath(request);
		if (modulePath.contains("/")) {
			return modulePath.split("/")[1];
		}
		return modulePath;
	}

	private String getModulePath(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = request.getContext().getContextPath();
		if (!"/".equals(contextPath) && path.startsWith(contextPath)) {
			path = path.replaceFirst(contextPath, "");
		}

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		return path;
	}
}
