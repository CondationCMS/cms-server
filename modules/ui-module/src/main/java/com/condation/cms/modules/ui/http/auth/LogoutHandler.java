package com.condation.cms.modules.ui.http.auth;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.module.SiteRequestContext;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.modules.ui.http.JettyHandler;
import com.condation.cms.modules.ui.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author thorstenmarx
 */
@RequiredArgsConstructor
@Slf4j
public class LogoutHandler extends JettyHandler {

	private final SiteModuleContext context;
	private final RequestContext requestContext;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		AuthUtil.logout(request, response, requestContext, context);

		response.setStatus(302);
		response.getHeaders().add("Location", managerURL("/manager/login", requestContext));
		callback.succeeded();

		return true;
	}

}
