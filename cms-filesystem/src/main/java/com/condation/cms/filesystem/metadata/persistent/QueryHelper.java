package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.filesystem.metadata.query.Queries;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Condition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.ContainsCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Expression;
import com.condation.cms.filesystem.metadata.query.parser.expressions.InCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Logical;
import com.condation.cms.filesystem.metadata.query.parser.expressions.LogicalOperator;
import com.condation.cms.filesystem.metadata.query.parser.values.Value;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class QueryHelper {

	public static void exists(BooleanQuery.Builder queryBuilder, String field) {
		queryBuilder.add(
				new TermQuery(new Term("_fields", field)),
				BooleanClause.Occur.FILTER);
	}

	public static void lt(BooleanQuery.Builder queryBuilder, String field, Object value) {
		switch (value) {
			case Integer numberValue -> {
				queryBuilder.add(
						IntField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 1),
						BooleanClause.Occur.MUST);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 1),
						BooleanClause.Occur.MUST);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 0.0001f),
						BooleanClause.Occur.MUST);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 0.0001),
						BooleanClause.Occur.MUST);
			}
			default -> {
			}
		}
	}

	public static void lte(BooleanQuery.Builder queryBuilder, String field, Object value) {
		switch (value) {
			case Integer numberValue -> {
				queryBuilder.add(
						IntField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST);
			}
			default -> {
			}
		}
	}

	public static void gt(BooleanQuery.Builder queryBuilder, String field, Object value) {
		switch (value) {
			case Integer numberValue -> {
				queryBuilder.add(
						IntField.newRangeQuery(field, numberValue + 1, Integer.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, numberValue + 1, Long.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, numberValue + 0.0001f, Float.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, numberValue + 0.0001, Double.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			default -> {
			}
		}
	}

	public static void gte(BooleanQuery.Builder queryBuilder, String field, Object value) {
		switch (value) {
			case Integer numberValue -> {
				queryBuilder.add(
						IntField.newRangeQuery(field, numberValue, Integer.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, numberValue, Long.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, numberValue, Float.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, numberValue, Double.MAX_VALUE),
						BooleanClause.Occur.MUST);
			}
			default -> {
			}
		}
	}

	public static void eq(BooleanQuery.Builder queryBuilder, String field, Object value, BooleanClause.Occur occur) {
		var query = toQuery(field, value);
		if (query != null) {
			queryBuilder.add(query, occur);
		}
	}

	public static void contains(BooleanQuery.Builder queryBuilder, String field, Object value,
			BooleanClause.Occur occur) {
		var query = toQuery(field, value);
		if (query != null) {
			queryBuilder.add(
					TermRangeQuery.newStringRange(field, null, null, true, true),
					BooleanClause.Occur.FILTER);
			queryBuilder.add(query, occur);
		}
	}

	public static void in(BooleanQuery.Builder queryBuilder, String field, Object value, BooleanClause.Occur occur) {
		if (value == null) {
			log.warn("value is null");
			return;
		}
		if (!(value instanceof List || value.getClass().isArray())) {
			log.warn("value is not of type list");
			return;
		}

		BooleanQuery.Builder inBuilder = new BooleanQuery.Builder();

		List<?> listValues = Collections.emptyList();
		if (value instanceof List) {
			listValues = (List<?>) value;
		} else if (value.getClass().isArray()) {
			listValues = Arrays.asList((Object[]) value);
		}

		listValues.forEach(item -> {
			inBuilder.add(toQuery(field, item), BooleanClause.Occur.SHOULD);
		});

		var inQuery = inBuilder.build();
		if (!inQuery.clauses().isEmpty()) {
			queryBuilder.add(inQuery, occur);
		}
	}

	private static Query toQuery(final String field, final Object value) {
		if (value instanceof String stringValue) {
			return new TermQuery(new Term(field, stringValue));
		} else if (value instanceof Boolean booleanValue) {
			return IntField.newExactQuery(
					field,
					booleanValue ? 1 : 0);
		} else if (value instanceof Integer numberValue) {
			return IntField.newExactQuery(field, numberValue);
		} else if (value instanceof Long numberValue) {
			return LongField.newExactQuery(field, numberValue);
		} else if (value instanceof Float numberValue) {
			return FloatField.newExactQuery(field, numberValue);
		} else if (value instanceof Double numberValue) {
			return DoubleField.newExactQuery(field, numberValue);
		}
		return null;
	}

	protected static <T extends ContentNode> List<T> sorted(final List<T> nodes, final String field, final boolean asc) {

		var tempNodes = nodes.stream().sorted(
				(node1, node2) -> {
					var value1 = MapUtil.getValue(((ContentNode) node1).data(), field);
					var value2 = MapUtil.getValue(((ContentNode) node2).data(), field);

					return Queries.compare(value1, value2);
				}).toList();

		if (!asc) {
			tempNodes = tempNodes.reversed();
		}

		return tempNodes;
	}

	public static void buildFromExpression(BooleanQuery.Builder queryBuilder, Expression expression) {
		if (expression instanceof Condition condition) {
			buildCondition(queryBuilder, condition);
		} else if (expression instanceof Logical logical) {
			// Subqueries bauen
			BooleanQuery.Builder leftBuilder = new BooleanQuery.Builder();
			buildFromExpression(leftBuilder, logical.left());

			BooleanQuery.Builder rightBuilder = new BooleanQuery.Builder();
			buildFromExpression(rightBuilder, logical.right());

			BooleanQuery subQuery = new BooleanQuery.Builder()
					.add(leftBuilder.build(), toOccur(logical.operator()))
					.add(rightBuilder.build(), toOccur(logical.operator()))
					.build();

			queryBuilder.add(subQuery, BooleanClause.Occur.MUST);
		} else if (expression instanceof InCondition inCondition) {
			buildInCondition(queryBuilder, inCondition);
		} else if (expression instanceof ContainsCondition containsCondition) {
			buildContainsCondition(queryBuilder, containsCondition);
		}
	}

	private static void buildInCondition(BooleanQuery.Builder queryBuilder, InCondition condition) {
		var field = condition.field();
		var not = condition.negated();

		exists(queryBuilder, field);
		
		var values = condition.values().stream().map(Value::get).toList();

		if (not) {
			in(queryBuilder, field, values, BooleanClause.Occur.MUST_NOT);
		} else {
			in(queryBuilder, field, values, BooleanClause.Occur.MUST);
		}
	}

	private static void buildContainsCondition(BooleanQuery.Builder queryBuilder, ContainsCondition condition) {
		var field = condition.field();
		var not = condition.negated();

		exists(queryBuilder, field);
		
		var values = condition.values().stream().map(Value::get).toList();
		if (values.isEmpty()) {
			return; // nichts zu tun
		}

		BooleanQuery.Builder containsBuilder = new BooleanQuery.Builder();
		for (Object item : values) {
			Query termQuery = toQuery(field, item);
			containsBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
		}

		Query containsQuery = containsBuilder.build();

		if (not) {
			// Variante A: KEINER der Werte darf vorkommen
			// also: Dokumente ausschließen, die einen Treffer hätten
			queryBuilder.add(containsQuery, BooleanClause.Occur.MUST_NOT);
		} else {
			// Variante B: Mindestens EIN Wert muss vorkommen
			queryBuilder.add(containsQuery, BooleanClause.Occur.MUST);
		}
	}

	private static void buildCondition(BooleanQuery.Builder queryBuilder, Condition condition) {
		String field = condition.field();
		Value value = condition.value();

		exists(queryBuilder, field);
		
		switch (condition.operator()) {
			case EQ ->
				eq(queryBuilder, field, value.get(), BooleanClause.Occur.MUST);
			case NEQ -> {
				BooleanQuery.Builder notBuilder = new BooleanQuery.Builder();
				eq(notBuilder, field, value.get(), BooleanClause.Occur.MUST);
				queryBuilder.add(notBuilder.build(), BooleanClause.Occur.MUST_NOT);
			}
			case GT ->
				gt(queryBuilder, field, value.get());
			case GTE ->
				gte(queryBuilder, field, value.get());
			case LT ->
				lt(queryBuilder, field, value.get());
			case LTE ->
				lte(queryBuilder, field, value.get());
			case IN ->
				in(queryBuilder, field, value.get(), BooleanClause.Occur.MUST);
			case NOT_IN -> {
				BooleanQuery.Builder notInBuilder = new BooleanQuery.Builder();
				in(notInBuilder, field, value.get(), BooleanClause.Occur.MUST);
				queryBuilder.add(notInBuilder.build(), BooleanClause.Occur.MUST_NOT);
			}
			case CONTAINS ->
				contains(queryBuilder, field, value.get(), BooleanClause.Occur.MUST);
			case CONTAINS_NOT -> {
				BooleanQuery.Builder notContains = new BooleanQuery.Builder();
				contains(notContains, field, value.get(), BooleanClause.Occur.MUST);
				queryBuilder.add(notContains.build(), BooleanClause.Occur.MUST_NOT);
			}
			default ->
				throw new UnsupportedOperationException("Unsupported operator: " + condition.operator());
		}
	}

	private static BooleanClause.Occur toOccur(LogicalOperator operator) {
		return switch (operator) {
			case AND ->
				BooleanClause.Occur.MUST;
			case OR ->
				BooleanClause.Occur.SHOULD;
		};
	}

}
