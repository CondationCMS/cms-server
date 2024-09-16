package com.condation.cms.server.filter;

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
import com.condation.cms.api.feature.Feature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.request.RequestContextFactory;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 *
 * @author t.marx
 */
@Slf4j
public class InitRequestContextFilter extends Handler.Abstract {

	private final RequestContextFactory requestContextFactory;

	public static final String REQUEST_CONTEXT = "_requestContext";

	@Inject
	public InitRequestContextFilter(final RequestContextFactory requestContextFactory) {
		super();
		this.requestContextFactory = requestContextFactory;
	}

	@Override
	public boolean handle(final Request httpRequest, final Response rspns, final Callback clbck) throws Exception {
		var requestContext = (RequestContext) httpRequest.getAttribute(REQUEST_CONTEXT);

		if (requestContext.has(AlreadyInitialized.class)) {
			return false;
		}
		requestContextFactory.initContext(requestContext, httpRequest);
		requestContext.add(AlreadyInitialized.class, new AlreadyInitialized());

		return false;
	}

	private record AlreadyInitialized() implements Feature {
	}
;

}
