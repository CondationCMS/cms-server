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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.db.ContentQuery;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.Page;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.filesystem.index.IndexProviding;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.filtered;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.filteredWithIndex;
import static com.github.thmarx.cms.filesystem.query.QueryUtil.sorted;
import com.github.thmarx.cms.filesystem.utils.NodeUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author t.marx
 */
public class Query<T> implements ContentQuery<T> {

//	private final Stream<ContentNode> nodes;
//
//	private ExcerptMapperFunction<T> nodeMapper;
//
//	private IndexProviding indexProviding;
//
//	@Getter
//	@Setter
//	private boolean useSecondaryIndex = false;
	private QueryContext<T> context;

	public Query(Collection<ContentNode> nodes, IndexProviding indexProviding, BiFunction<ContentNode, Integer, T> nodeMapper) {
		this(nodes.stream(), indexProviding, new ExcerptMapperFunction<>(nodeMapper));
	}

	public Query(Collection<ContentNode> nodes, IndexProviding indexProviding, ExcerptMapperFunction<T> nodeMapper) {
		this(nodes.stream(), indexProviding, nodeMapper);
	}

	public Query(Stream<ContentNode> nodes, IndexProviding indexProviding, BiFunction<ContentNode, Integer, T> nodeMapper) {
		this(nodes, indexProviding, new ExcerptMapperFunction<>(nodeMapper));
	}

	public Query(Stream<ContentNode> nodes, IndexProviding indexProviding, ExcerptMapperFunction<T> nodeMapper) {
		this(new QueryContext(nodes, nodeMapper, indexProviding, false, Constants.DEFAULT_CONTENT_TYPE));
	}

	public Query(QueryContext<T> context) {
		this.context = context;
	}

	@Override
	public Query<T> excerpt(final int excerptLength) {
		context.getNodeMapper().setExcerpt(excerptLength);
		return this;
	}

	@Override
	public Query<T> where(final String field, final Object value) {
		return where(field, QueryUtil.Operator.EQ, value);
	}

	@Override
	public Query<T> where(final String field, final String operator, final Object value) {
		return where(field, QueryUtil.operator4String(operator), value);
	}

	@Override
	public Query<T> whereContains(final String field, final Object value) {
		return where(field, QueryUtil.Operator.CONTAINS, value);
	}

	@Override
	public Query<T> whereNotContains(final String field, final Object value) {
		return where(field, QueryUtil.Operator.CONTAINS_NOT, value);
	}

	@Override
	public Query<T> whereIn(final String field, final Object... value) {
		return where(field, QueryUtil.Operator.IN, value);
	}

	@Override
	public Query<T> whereNotIn(final String field, final Object... value) {
		return where(field, QueryUtil.Operator.NOT_IN, value);
	}

	@Override
	public Query<T> whereIn(final String field, final List<Object> value) {
		return where(field, QueryUtil.Operator.IN, value);
	}

	@Override
	public Query<T> whereNotIn(final String field, final List<Object> value) {
		return where(field, QueryUtil.Operator.NOT_IN, value);
	}

	private Query<T> where(final String field, final QueryUtil.Operator operator, final Object value) {
		if (context.isUseSecondaryIndex()) {
			return new Query(filteredWithIndex(context, field, value, operator));
		} else {
			return new Query(filtered(context, field, value, operator));
		}
	}

	public Query<T> enableSecondaryIndex() {
		context.setUseSecondaryIndex(true);
		return new Query<>(context);
	}
	
	public Query<T> html() {
		context.setContentType(Constants.ContentTypes.HTML);
		return new Query<>(context);
	}

	public Query<T> json() {
		context.setContentType(Constants.ContentTypes.JSON);
		return new Query<>(context);
	}

	@Override
	public int count() {
		return (int) context.getNodes()
				.filter(NodeUtil.contentTypeFiler(context.getContentType()))
				.count();
	}

	@Override
	public List<T> get(final long offset, final long size) {
		return get((int) offset, (int) size);
	}

	@Override
	public List<T> get(final int offset, final int size) {
		return context.getNodes()
				.filter(NodeUtil.contentTypeFiler(context.getContentType()))
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.skip(offset)
				.limit(size)
				.map(context.getNodeMapper())
				.toList();
	}

	public Page<T> page(final Object page, final Object size) {
		int i_page = Constants.DEFAULT_PAGE;
		int i_size = Constants.DEFAULT_PAGE_SIZE;
		if (page instanceof Integer || page instanceof Long) {
			i_page = ((Number) page).intValue();
		} else if (page instanceof String) {
			i_page = Integer.valueOf((String) page);
		}
		if (size instanceof Integer || size instanceof Long) {
			i_size = ((Number) size).intValue();
		} else if (size instanceof String) {
			i_size = Integer.valueOf((String) size);
		}
		return page((int) i_page, (int) i_size);
	}

	public Page<T> page(final long page, final long size) {
		return page((int) page, (int) size);
	}

	public Page<T> page(final int page, final int size) {
		int total = count();
		int offset = (page - 1) * size;

		var filteredNodes = context.getNodes()
				.filter(NodeUtil.contentTypeFiler(context.getContentType()))
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.skip(offset)
				.limit(size)
				.map(context.getNodeMapper())
				.toList();

		int totalPages = (int) Math.ceil((float) total / size);
		return new Page<T>(filteredNodes.size(), totalPages, page, filteredNodes);
	}

	@Override
	public List<T> get() {
		return context.getNodes()
				.filter(NodeUtil.contentTypeFiler(context.getContentType()))
				.filter(node -> !node.isDirectory())
				.filter(MetaData::isVisible)
				.map(context.getNodeMapper())
				.toList();
	}

	@Override
	public Sort<T> orderby(final String field) {
		return new Sort<T>(field, context);
	}

	@Override
	public Map<Object, List<ContentNode>> groupby(final String field) {
		return QueryUtil.groupby(context.getNodes(), field);
	}

	public static record Sort<T>(String field, QueryContext context) implements ContentQuery.Sort<T> {

		public Query<T> asc() {
			return new Query(sorted(context, field, true));
		}

		public Query<T> desc() {
			return new Query(sorted(context, field, false));
		}
	}
}
