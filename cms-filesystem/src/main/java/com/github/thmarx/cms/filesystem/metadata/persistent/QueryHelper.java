package com.github.thmarx.cms.filesystem.metadata.persistent;

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
	public static void lt(BooleanQuery.Builder queryBuilder, String field, Object value) {
		switch (value) {
			case Integer numberValue -> {
				queryBuilder.add(
						IntField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 1),
						BooleanClause.Occur.MUST
				);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 1),
						BooleanClause.Occur.MUST
				);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 0.0001f),
						BooleanClause.Occur.MUST
				);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue - 0.0001),
						BooleanClause.Occur.MUST
				);
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
						BooleanClause.Occur.MUST
				);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST
				);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST
				);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue),
						BooleanClause.Occur.MUST
				);
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
						BooleanClause.Occur.MUST
				);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, numberValue + 1, Long.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, numberValue + 0.0001f, Float.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, numberValue + 0.0001, Double.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
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
						BooleanClause.Occur.MUST
				);
			}
			case Long numberValue -> {
				queryBuilder.add(
						LongField.newRangeQuery(field, numberValue, Long.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
			}
			case Float numberValue -> {
				queryBuilder.add(
						FloatField.newRangeQuery(field, numberValue, Float.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
			}
			case Double numberValue -> {
				queryBuilder.add(
						DoubleField.newRangeQuery(field, numberValue, Double.MAX_VALUE),
						BooleanClause.Occur.MUST
				);
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

	public static void contains(BooleanQuery.Builder queryBuilder, String field, Object value, BooleanClause.Occur occur) {
		var query = toQuery(field, value);
		if (query != null) {
			queryBuilder.add(
					TermRangeQuery.newStringRange(field, null, null, true, true),
					BooleanClause.Occur.FILTER
			);
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
					booleanValue ? 1 : 0
			);
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
}
