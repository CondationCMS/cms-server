package com.github.thmarx.cms.modules.search.index;

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


import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ThorstenMarx
 */
public class SearchResult {

	@Getter
	public final List<Item> items;
	
	@Getter
	@Setter
	public long total;
	
	public SearchResult () {
		items = new ArrayList<>();
	}
	
	@Data
	public static class Item {
		public String uri;
		public String title;
		public String content;
	}
	
}

