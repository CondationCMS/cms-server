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
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class Query<T> {

	private final Collection<MetaData.MetaNode> nodes;
	public final Function<MetaData.MetaNode, T> nodeMapper;

	public Where<T> where(final String field) {
		return new Where<T>(field, nodes, nodeMapper);
	}

	public List<T> get(final int offset, final int size) {
		var filteredNodes = nodes.stream()
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.skip(offset)
				.limit(size)
				.toList();
		return Collections.unmodifiableList(filteredNodes.stream().map(nodeMapper).toList());
	}
	
	public List<T> get() {
		var filteredNodes = nodes.stream()
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.toList();
		return Collections.unmodifiableList(filteredNodes.stream().map(nodeMapper).toList());
	}
	
	public Sort<T> sort (final String field) {
		return new Sort<T>(field, nodes, nodeMapper);
	}

	

	public static record Where<T>(String field, Collection<MetaData.MetaNode> nodes, Function<MetaData.MetaNode, T> nodeMapper) {

		public Query<T> not(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.NOT_EQUALS), nodeMapper);
		}

		public Query<T> is(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.EQUALS), nodeMapper);
		}
		
		public Query<T> contains(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.CONTAINS), nodeMapper);
		}
		public Query<T> contains_not(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.CONTAINS_NOT), nodeMapper);
		}
	}
	public static record Sort<T>(String field, Collection<MetaData.MetaNode> nodes, Function<MetaData.MetaNode, T> nodeMapper) {

		public Query<T> asc() {
			return new Query<>(sorted(nodes, field, true), nodeMapper);
		}

		public Query<T> desc() {
			return new Query<>(sorted(nodes, field, false), nodeMapper);
		}
	}
}
