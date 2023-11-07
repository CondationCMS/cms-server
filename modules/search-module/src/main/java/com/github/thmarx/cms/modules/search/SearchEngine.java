package com.github.thmarx.cms.modules.search;

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
