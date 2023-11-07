package com.github.thmarx.cms.modules.search.http;

/*-
 * #%L
 * search-module
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

import com.github.thmarx.cms.modules.search.SearchEngine;
import com.github.thmarx.cms.modules.search.SearchRequest;
import com.github.thmarx.cms.modules.search.index.SearchResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class SearchHandler extends Handler.Abstract {
	protected static final String PARAMETER_QUERY = "query";
	protected static final String PARAMETER_PAGE = "page";
	protected static final String PARAMETER_SIZE = "size";
	

	private final SearchEngine searchEngine;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception {

		Fields extractQueryParameters = Request.extractQueryParameters(request, StandardCharsets.UTF_8);
		final String query = extractQueryParameters.get(PARAMETER_QUERY) != null ? extractQueryParameters.get(PARAMETER_QUERY).getValue() : "";
		final int page = extractQueryParameters.get(PARAMETER_PAGE) != null ? extractQueryParameters.get(PARAMETER_PAGE).getValueAsInt() : 1;
		final int size = extractQueryParameters.get(PARAMETER_SIZE) != null ? extractQueryParameters.get(PARAMETER_SIZE).getValueAsInt() : 10;

		SearchRequest searchRequest = new SearchRequest(query, page, size);
		SearchResult searchResult = searchEngine.search(searchRequest);
		
		Content.Sink.write(response, true, GSON.toJson(searchResult), callback);
		return true;
	}

}
