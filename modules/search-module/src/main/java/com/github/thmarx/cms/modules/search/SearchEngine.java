package com.github.thmarx.cms.modules.search;

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

import com.github.thmarx.cms.modules.search.index.SearchIndex;
import com.github.thmarx.cms.modules.search.index.SearchResult;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class SearchEngine implements AutoCloseable{

	private SearchIndex searchIndex;
	
	public void open (Path path, String language) throws IOException {
		
		searchIndex = new SearchIndex(path, language);
		searchIndex.open();
	}
	
	@Override
	public void close() throws Exception {
		if (searchIndex != null) {
			searchIndex.close();
		}
	}
	
	public void commit () throws IOException {
		searchIndex.commit();
	}
	
	public void index (IndexDocument document) {
		try {
			searchIndex.index(document);
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}
	
	public SearchResult search (SearchRequest request)  {
		if (Strings.isNullOrEmpty(request.query())) {
			return new SearchResult();
		}
		try {
			return searchIndex.search(request);
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return new SearchResult();
	}

	public void clear() {
		try {
			searchIndex.clear();
		} catch (IOException ex) {
			log.error(null, ex);
		}
	}
	
}
