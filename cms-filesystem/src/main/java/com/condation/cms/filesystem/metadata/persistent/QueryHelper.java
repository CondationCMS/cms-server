package com.condation.cms.filesystem.metadata.persistent;

/*-
 * #%L
 * CMS FileSystem
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Date;
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
            case Number numberValue -> {
                queryBuilder.add(
                        DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue.doubleValue() - 0.0001),
                        BooleanClause.Occur.MUST);
            }
            default -> {
            }
        }
    }

    public static void lte(BooleanQuery.Builder queryBuilder, String field, Object value) {
        switch (value) {
            case Number numberValue -> {
                queryBuilder.add(
                        DoubleField.newRangeQuery(field, Integer.MIN_VALUE, numberValue.doubleValue()),
                        BooleanClause.Occur.MUST);
            }
            default -> {
            }
        }
    }

    public static void gt(BooleanQuery.Builder queryBuilder, String field, Object value) {
        switch (value) {
            case Number numberValue -> {
                queryBuilder.add(
                        DoubleField.newRangeQuery(field, numberValue.doubleValue() + 0.0001, Double.MAX_VALUE),
                        BooleanClause.Occur.MUST);
            }
            default -> {
            }
        }
    }

    public static void gte(BooleanQuery.Builder queryBuilder, String field, Object value) {
        switch (value) {
            case Number numberValue -> {
                queryBuilder.add(
                        DoubleField.newRangeQuery(field, numberValue.doubleValue(), Double.MAX_VALUE),
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

    static Query toQuery(final String field, final Object value) {
        
        if (value instanceof String stringValue) {
            return new TermQuery(new Term(field, stringValue));
        } else if (value instanceof Boolean booleanValue) {
            return IntField.newExactQuery(
                    field,
                    booleanValue ? 1 : 0);
        } else if (value instanceof Number numberValue) {
            return DoubleField.newExactQuery(field, numberValue.doubleValue());
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

    /**
     * Hilfsmethode zur Bestimmung des tatsächlichen Lucene-Feldnamens
     */
    public static String resolveFieldName(String field, Queries.Operator operator, Object value) {
        if (value == null) {
            return field;
        }

        // Bestimme, ob es sich um eine mathematische Range-Query (><) handelt
        boolean isRangeQuery = switch (operator) {
            case LT, LTE, GT, GTE ->
                true;
            default ->
                false;
        };

        // Pattern Matching passend zu unserem addValue2 Indizierungs-Schema
        return switch (value) {
            case Number n ->
                isRangeQuery || operator == Queries.Operator.EQ
                ? "%s_double".formatted(field)
                : field;

            case Boolean b ->
                "%s_bool".formatted(field);

            case Date d ->
                "%s_date".formatted(field);

            // Bei Listen (z.B. für den IN-Operator) bestimmen wir den Typ anhand des ersten Elements
            case List<?> listValue when !listValue.isEmpty() ->
                resolveFieldName(field, operator, listValue.get(0));

            default ->
                field; // Strings und unbekannte Typen nutzen immer das Hauptfeld
        };
    }

}
