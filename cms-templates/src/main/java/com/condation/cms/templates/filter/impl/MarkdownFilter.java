package com.condation.cms.templates.filter.impl;

import com.condation.cms.api.feature.features.MarkdownRendererFeature;
import com.condation.cms.api.request.RequestContextScope;
import com.condation.cms.templates.filter.Filter;

/**
 *
 * @author thmar
 */
public class MarkdownFilter implements Filter {
	
	public static final String NAME = "markdown";

	@Override
	public Object apply(Object input, Object... params) {
		if (input == null || !(input instanceof String)) {
			return input;
		}

		var requestContext = RequestContextScope.REQUEST_CONTEXT.get();

		if (requestContext.has(MarkdownRendererFeature.class)) {
			return requestContext.get(MarkdownRendererFeature.class).markdownRenderer().render((String)input);
		}

		return input;
	}
}
