package com.condation.cms.server.handler;

/*-
 * #%L
 * cms-server
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author thorstenmarx
 */
public abstract class AbstractHandler extends Handler.Abstract {

	protected boolean isPreview(Request request) {
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);
		return requestContext.has(IsPreviewFeature.class);
	}
}
