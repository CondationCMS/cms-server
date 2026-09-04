package com.condation.cms.server.handler.content;

/*-
 * #%L
 * CMS Server
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import com.condation.cms.api.content.DefaultContentResponse;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.content.CollectionResolver;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * Serves collection items whose detail routes are configured for the site.
 */
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class JettyCollectionHandler extends Handler.Abstract {

	private final CollectionResolver collectionResolver;

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {
		var requestContext = (RequestContext) request.getAttribute(Constants.REQUEST_CONTEXT_ATTRIBUTE_NAME);
		var resolvedContent = collectionResolver.getContent(requestContext);
		if (resolvedContent.isEmpty()) {
			return false;
		}

		var content = (DefaultContentResponse) resolvedContent.get();
		response.setStatus(200);
		response.getHeaders().add(
				HttpHeader.CONTENT_TYPE,
				"%s; charset=utf-8".formatted(content.contentType()));
		Content.Sink.write(response, true, content.content(), callback);
		return true;
	}
}
