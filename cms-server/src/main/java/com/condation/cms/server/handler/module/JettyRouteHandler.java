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


import com.condation.cms.api.extensions.HttpRouteExtensionPoint;
import com.condation.cms.api.utils.RequestUtil;
import com.condation.modules.api.ModuleManager;
import com.google.inject.Inject;
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
@RequiredArgsConstructor(onConstructor = @__({
	@Inject}))
@Slf4j
public class JettyRouteHandler extends Handler.Abstract {

	private final ModuleManager moduleManager;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		String route = RequestUtil.getContentPath(request);

		Optional<HttpRouteExtensionPoint> findRoute = moduleManager.extensions(HttpRouteExtensionPoint.class).stream()
				.filter(extension -> extension.getRoute().equals(route)).findFirst();

		if (findRoute.isPresent()) {
			findRoute.get().handle(request, response, callback);
			return true;
		}

		return false;
	}
}
