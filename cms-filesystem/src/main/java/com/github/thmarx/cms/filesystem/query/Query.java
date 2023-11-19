package com.github.thmarx.cms.filesystem.query;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.filesystem.MetaData;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.filtered;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.sorted;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

	public List<T> get(final long offset, final long size) {
		return get((int)offset, (int)size);
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
	
	public Sort<T> orderby (final String field) {
		return new Sort<T>(field, nodes, nodeMapper);
	}
	
	public Map<Object, List<MetaData.MetaNode>> groupby (final String field) {
		return QueryUtil.groupby(nodes, field);
	}

	

	public static record Where<T>(String field, Collection<MetaData.MetaNode> nodes, Function<MetaData.MetaNode, T> nodeMapper) {
		
		public Query<T> not_eq(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.NOT_EQ), nodeMapper);
		}

		public Query<T> eq(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.EQ), nodeMapper);
		}
		
		public Query<T> contains(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.CONTAINS), nodeMapper);
		}
		public Query<T> contains_not(Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.CONTAINS_NOT), nodeMapper);
		}
		
		public Query<T> gt (Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.GT), nodeMapper);
		}
		public Query<T> gte (Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.GTE), nodeMapper);
		}
		
		public Query<T> lt (Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.LT), nodeMapper);
		}
		public Query<T> lte (Object value) {
			return new Query<>(filtered(nodes, field, value, QueryUtil.Operator.LTE), nodeMapper);
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
