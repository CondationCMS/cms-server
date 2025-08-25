package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.Constants;
import com.condation.cms.api.feature.features.SitePropertiesFeature;
import com.condation.cms.api.request.RequestContext;
import java.net.InetSocketAddress;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author t.marx
 */
public class RequestUtil {

	public static String getContextPath(Request request) {
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);
		return requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
	}
	
	/**
	 * removes the context from the path
	 * @param request
	 * @return 
	 */
	public static String getContentPath(Request request) {
		var path = request.getHttpURI().getPath();
		var contextPath = getContextPath(request);
		if (!"/".equals(contextPath) && path.startsWith(contextPath)) {
			path = path.replaceFirst(contextPath, "");
		}

		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		return path;
	}
	
	public static String clientAddress(Request request) {
		String forwarded = request.getHeaders().get(HttpHeader.X_FORWARDED_FOR);
		if (forwarded != null && !forwarded.isEmpty()) {
			return forwarded.split(",")[0].trim();
		}
		return ((InetSocketAddress) request.getConnectionMetaData().getRemoteSocketAddress())
				.getAddress().getHostAddress();
	}
}
