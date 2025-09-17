package com.condation.cms.content.markdown;

import com.condation.cms.api.feature.features.IsPreviewFeature;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.request.RequestContextScope;
import java.util.Optional;

/*-
 * #%L
 * cms-content
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
/**
 *
 * @author t.marx
 */
public interface InlineBlock {

	int start();

	int end();

	String render();

	default boolean isPreview() {
		if (!RequestContextScope.REQUEST_CONTEXT.isBound()) {
			return false;
		}
		var requestContext = RequestContextScope.REQUEST_CONTEXT.get();
		return requestContext != null && requestContext.has(IsPreviewFeature.class);
	}
	
	default Optional<RequestContext> getRequestContext () {
		if (!RequestContextScope.REQUEST_CONTEXT.isBound()) {
			return Optional.empty();
		}
		return Optional.ofNullable(RequestContextScope.REQUEST_CONTEXT.get());
	}
}
