package com.github.thmarx.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.ContentQuery;
import com.github.thmarx.cms.api.db.Page;
import com.github.thmarx.cms.filesystem.MetaData;
import com.github.thmarx.cms.filesystem.metadata.AbstractMetaData;
import com.github.thmarx.cms.filesystem.metadata.query.ExcerptMapperFunction;
import com.github.thmarx.cms.filesystem.metadata.query.Queries;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class LuceneQuery<T> implements ContentQuery<T>, ContentQuery.Sort<T> {

	private final LuceneIndex index;
	private final MetaData metaData;
	private final ExcerptMapperFunction<T> nodeMapper;

	private String contentType = Constants.DEFAULT_CONTENT_TYPE;

	private final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

	enum Order {
		ASC, DESC;
	}

	private Order sortOrder = Order.ASC;
	private Optional<String> orderByField = Optional.empty();

	@Override
	public ContentQuery<T> excerpt(long excerptLength) {
		nodeMapper.setExcerpt((int) excerptLength);
		return this;
	}

	public Page<T> page(final Object page, final Object size) {
		int i_page = Constants.DEFAULT_PAGE;
		int i_size = Constants.DEFAULT_PAGE_SIZE;
		if (page instanceof Integer || page instanceof Long) {
			i_page = ((Number) page).intValue();
		} else if (page instanceof String string) {
			i_page = Integer.parseInt(string);
		}
		if (size instanceof Integer || size instanceof Long) {
			i_size = ((Number) size).intValue();
		} else if (size instanceof String string) {
			i_size = Integer.parseInt(string);
		}
		return page((int) i_page, (int) i_size);
	}

	@Override
	public Page<T> page(long page, long size) {

		long offset = (page - 1) * size;

		queryBuilder.add(new TermQuery(new Term("content.type", contentType)), BooleanClause.Occur.MUST);

		try {
			var result = index.query(queryBuilder.build());

			var filteredNodes = result.stream()
					.map(document -> document.get("_uri"))
					.map(metaData::byUri)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(node -> !node.isDirectory())
					.filter(AbstractMetaData::isVisible)
					.map(nodeMapper)
					.toList();

			var total = filteredNodes.size();

			if (orderByField.isPresent()) {
				filteredNodes = (List<T>)QueryHelper.sorted(filteredNodes, orderByField.get(), Order.ASC.equals(sortOrder));
			}
			
			var filteredTargetNodes = filteredNodes.stream()
					.skip(offset)
					.limit(size)
					.toList();

			int totalPages = (int) Math.ceil((float) total / size);
			return new Page<>(filteredNodes.size(), totalPages, (int) page, filteredTargetNodes);

		} catch (IOException ex) {
			log.error("", ex);
		}

		return Page.EMPTY;
	}

	@Override
	public List<T> get() {
		queryBuilder.add(new TermQuery(new Term("content.type", contentType)), BooleanClause.Occur.MUST);

		try {
			List<Document> result;
//			if (orderByField.isPresent()) {
//				org.apache.lucene.search.Sort sort = new org.apache.lucene.search.Sort(
//						new SortField("year", SortField.Type.INT, true)
//				);
//			} else {
				result = index.query(queryBuilder.build());
//			}

			var nodes = result.stream()
					.map(document -> document.get("_uri"))
					.map(metaData::byUri)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(node -> !node.isDirectory())
					.filter(AbstractMetaData::isVisible)
					.map(nodeMapper).toList();
			if (orderByField.isPresent()) {
				return (List<T>)QueryHelper.sorted(nodes, orderByField.get(), Order.ASC.equals(sortOrder));
			} else {
				return nodes;
			}
		} catch (IOException ex) {
			log.error("", ex);
		}

		return Collections.emptyList();
	}

	@Override
	public Map<Object, List<ContentNode>> groupby(String field) {
		return Collections.emptyMap();
	}

	@Override
	public Sort<T> orderby(String field) {
		this.orderByField = Optional.ofNullable(field);
		return this;
	}

	@Override
	public ContentQuery<T> json() {
		this.contentType = Constants.ContentTypes.JSON;
		return this;
	}

	@Override
	public ContentQuery<T> html() {
		this.contentType = Constants.ContentTypes.HTML;
		return this;
	}

	@Override
	public ContentQuery<T> contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public ContentQuery<T> where(String field, Object value) {
		return where(field, Queries.Operator.EQ, value);
	}

	@Override
	public ContentQuery<T> where(String field, String operator, Object value) {
		if (Queries.isDefaultOperation(operator)) {
			return where(field, Queries.operator4String(operator), value);
		}
		throw new IllegalArgumentException("unknown operator " + operator);
	}

	@Override
	public ContentQuery<T> whereContains(String field, Object value) {
		return where(field, Queries.Operator.CONTAINS, value);
	}

	@Override
	public ContentQuery<T> whereNotContains(String field, Object value) {
		return where(field, Queries.Operator.CONTAINS_NOT, value);
	}

	@Override
	public ContentQuery<T> whereIn(String field, Object... value) {
		return where(field, Queries.Operator.IN, value);
	}

	@Override
	public ContentQuery<T> whereIn(String field, List<Object> value) {
		return where(field, Queries.Operator.IN, value);
	}

	@Override
	public ContentQuery<T> whereNotIn(String field, Object... value) {
		return where(field, Queries.Operator.NOT_IN, value);
	}

	@Override
	public ContentQuery<T> whereNotIn(String field, List<Object> value) {
		return where(field, Queries.Operator.NOT_IN, value);
	}

	private ContentQuery<T> where(final String field, final Queries.Operator operator, final Object value) {

		switch (operator) {
			case EQ ->
				QueryHelper.eq(queryBuilder, field, value, BooleanClause.Occur.MUST);
			case NOT_EQ ->
				QueryHelper.eq(queryBuilder, field, value, BooleanClause.Occur.MUST_NOT);
			case CONTAINS ->
				QueryHelper.contains(queryBuilder, field, value, BooleanClause.Occur.MUST);
			case CONTAINS_NOT ->
				QueryHelper.contains(queryBuilder, field, value, BooleanClause.Occur.MUST_NOT);
			case IN ->
				QueryHelper.in(queryBuilder, field, value, BooleanClause.Occur.MUST);
			case NOT_IN ->
				QueryHelper.in(queryBuilder, field, value, BooleanClause.Occur.MUST_NOT);
			case LT ->
				QueryHelper.lt(queryBuilder, field, value);
			case LTE ->
				QueryHelper.lte(queryBuilder, field, value);
			case GT ->
				QueryHelper.gt(queryBuilder, field, value);
			case GTE ->
				QueryHelper.gte(queryBuilder, field, value);
		}

		return this;
	}

	@Override
	public ContentQuery<T> asc() {
		this.sortOrder = Order.ASC;
		return this;
	}

	@Override
	public ContentQuery<T> desc() {
		this.sortOrder = Order.DESC;
		return this;
	}

}
