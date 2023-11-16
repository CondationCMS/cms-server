package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.filesystem.MetaData;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.filtered;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.sorted;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class Query {

	private final Collection<MetaData.MetaNode> nodes;

	public Where where(final String field) {
		return new Where(field, nodes);
	}

	public List<MetaData.MetaNode> get(final int offset, final int size) {
		var filteredNodes = nodes.stream()
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.skip(offset)
				.limit(size)
				.toList();
		return Collections.unmodifiableList(filteredNodes);
	}
	
	public List<MetaData.MetaNode> get() {
		var filteredNodes = nodes.stream()
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.toList();
		return Collections.unmodifiableList(filteredNodes);
	}
	
	public Sort sort (final String field) {
		return new Sort(field, nodes);
	}

	

	public static record Where(String field, Collection<MetaData.MetaNode> nodes) {

		public Query not(Object value) {
			return new Query(filtered(nodes, field, value, false));
		}

		public Query is(Object value) {
			return new Query(filtered(nodes, field, value, true));
		}
	}
	public static record Sort(String field, Collection<MetaData.MetaNode> nodes) {

		public Query asc() {
			return new Query(sorted(nodes, field, true));
		}

		public Query desc() {
			return new Query(sorted(nodes, field, false));
		}
	}
}
