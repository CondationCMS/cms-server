package com.condation.cms.content.markdown;

/*-
 * #%L
 * CMS Content
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

import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import java.util.Optional;
/**
 *
 * @author t.marx
 */
public interface InlineBlock {

	int start();

	int end();

	String render();

	/**
	 * Renders with absolute document positions. Override for elements that need
	 * to embed position metadata (e.g. images). Defaults to {@link #render()}.
	 */
	default String render(int absoluteStart, int absoluteEnd) {
		return render();
	}

	default boolean isPreview() {
		if (!RequestContextScope.REQUEST_CONTEXT.isBound()) {
			return false;
		}
		var requestContext = RequestContextScope.REQUEST_CONTEXT.get();
		return requestContext != null && requestContext.has(IsPreviewFeature.class);
	}
	
	default boolean isManagerPreview() {
		if (!RequestContextScope.REQUEST_CONTEXT.isBound()) {
			return false;
		}
		var requestContext = RequestContextScope.REQUEST_CONTEXT.get();
		if (requestContext == null || !requestContext.has(IsPreviewFeature.class)) {
			return false;
		}
		return IsPreviewFeature.Mode.MANAGER.equals(requestContext.get(IsPreviewFeature.class).mode());
	}
	
	default Optional<RequestContext> getRequestContext () {
		if (!RequestContextScope.REQUEST_CONTEXT.isBound()) {
			return Optional.empty();
		}
		return Optional.ofNullable(RequestContextScope.REQUEST_CONTEXT.get());
	}
}
