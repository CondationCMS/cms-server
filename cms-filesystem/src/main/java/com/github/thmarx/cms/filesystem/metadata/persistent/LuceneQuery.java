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
import com.github.thmarx.cms.filesystem.query.ExcerptMapperFunction;
import com.github.thmarx.cms.filesystem.query.QueryUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class LuceneQuery<T> implements ContentQuery<T> {

	private final LuceneIndex index;
	private final MetaData metaData;
	private final ExcerptMapperFunction<T> nodeMapper;

	private String contentType = Constants.DEFAULT_CONTENT_TYPE;

	private final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

	@Override
	public ContentQuery<T> excerpt(long excerptLength) {
		nodeMapper.setExcerpt((int) excerptLength);
		return this;
	}

	@Override
	public Page<T> page(long page, long size) {
		return null;
	}

	@Override
	public List<T> get() {
		queryBuilder.add(new TermQuery(new Term("content.type", contentType)), BooleanClause.Occur.MUST);

		try {
			var result = index.query(queryBuilder.build());

			return result.stream()
					.map(document -> document.get("_uri"))
					.map(metaData::byUri)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(node -> !node.isDirectory())
					.filter(AbstractMetaData::isVisible)
					.map(nodeMapper).toList();
		} catch (IOException ex) {
			Logger.getLogger(LuceneQuery.class.getName()).log(Level.SEVERE, null, ex);
		}

		return Collections.emptyList();
	}

	@Override
	public Map<Object, List<ContentNode>> groupby(String field) {
		return Collections.emptyMap();
	}

	@Override
	public Sort<T> orderby(String field) {
		return null;
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
		return where(field, QueryUtil.Operator.EQ, value);
	}

	@Override
	public ContentQuery<T> where(String field, String operator, Object value) {
		if (QueryUtil.isDefaultOperation(operator)) {
			return where(field, QueryUtil.operator4String(operator), value);
		}
		throw new IllegalArgumentException("unknown operator " + operator);
	}

	@Override
	public ContentQuery<T> whereContains(String field, Object value) {
		return where(field, QueryUtil.Operator.CONTAINS, value);
	}

	@Override
	public ContentQuery<T> whereNotContains(String field, Object value) {
		return where(field, QueryUtil.Operator.CONTAINS_NOT, value);
	}

	@Override
	public ContentQuery<T> whereIn(String field, Object... value) {
		return where(field, QueryUtil.Operator.IN, value);
	}

	@Override
	public ContentQuery<T> whereIn(String field, List<Object> value) {
		return where(field, QueryUtil.Operator.IN, value);
	}

	@Override
	public ContentQuery<T> whereNotIn(String field, Object... value) {
		return where(field, QueryUtil.Operator.NOT_IN, value);
	}

	@Override
	public ContentQuery<T> whereNotIn(String field, List<Object> value) {
		return where(field, QueryUtil.Operator.NOT_IN, value);
	}

	private ContentQuery<T> where(final String field, final QueryUtil.Operator operator, final Object value) {

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

	
}
