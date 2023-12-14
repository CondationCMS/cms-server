package com.github.thmarx.cms.modules.search.http;

/*-
 * #%L
 * search-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
