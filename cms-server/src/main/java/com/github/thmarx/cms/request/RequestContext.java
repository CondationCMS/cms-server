package com.github.thmarx.cms.request;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public record RequestContext (
		String uri, 
		Map<String, List<String>> queryParameters, 
		RequestExtensions extensions,
		RenderContext renderContext
		) implements AutoCloseable {
	
	public String getQueryParameter(String name, final String defaultValue) {
		if (!queryParameters.containsKey(name)) {
			return defaultValue;
		}

		return queryParameters.get(name).getFirst();
	}

	public int getQueryParameterAsInt(String name, final int defaultValue) {
		if (!queryParameters.containsKey(name)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(queryParameters.get(name).getFirst());
		} catch (Exception e) {
			log.error(null, e);
		}
		return defaultValue;
	}
	
	@Override
	public void close () throws Exception {
		extensions.close();
		renderContext.markdownRenderer().close();
	}
}
