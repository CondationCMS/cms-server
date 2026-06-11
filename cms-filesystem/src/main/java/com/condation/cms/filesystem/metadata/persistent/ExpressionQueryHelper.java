package com.condation.cms.filesystem.metadata.persistent;

import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.contains;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.eq;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.exists;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.gt;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.gte;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.in;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.lt;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.lte;
import static com.condation.cms.filesystem.metadata.persistent.QueryHelper.resolveFieldName;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Condition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.ContainsCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Expression;
import com.condation.cms.filesystem.metadata.query.parser.expressions.InCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Logical;
import com.condation.cms.filesystem.metadata.query.parser.expressions.LogicalOperator;
import com.condation.cms.filesystem.metadata.query.parser.values.Value;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

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

/**
 *
 * @author thmar
 */
public class ExpressionQueryHelper {
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
            Query termQuery = QueryHelper.toQuery(field, item);
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

        final String targetField = resolveFieldName(field, condition.operator().toQueriesOperator(), value.get());
        
        exists(queryBuilder, field);

        switch (condition.operator()) {
            case EQ ->
                eq(queryBuilder, targetField, value.get(), BooleanClause.Occur.MUST);
            case NEQ -> {
                BooleanQuery.Builder notBuilder = new BooleanQuery.Builder();
                eq(notBuilder, targetField, value.get(), BooleanClause.Occur.MUST);
                queryBuilder.add(notBuilder.build(), BooleanClause.Occur.MUST_NOT);
            }
            case GT ->
                gt(queryBuilder, targetField, value.get());
            case GTE ->
                gte(queryBuilder, targetField, value.get());
            case LT ->
                lt(queryBuilder, targetField, value.get());
            case LTE ->
                lte(queryBuilder, targetField, value.get());
            case IN ->
                in(queryBuilder, targetField, value.get(), BooleanClause.Occur.MUST);
            case NOT_IN -> {
                BooleanQuery.Builder notInBuilder = new BooleanQuery.Builder();
                in(notInBuilder, targetField, value.get(), BooleanClause.Occur.MUST);
                queryBuilder.add(notInBuilder.build(), BooleanClause.Occur.MUST_NOT);
            }
            case CONTAINS ->
                contains(queryBuilder, field, value.get(), BooleanClause.Occur.MUST);
            case CONTAINS_NOT -> {
                BooleanQuery.Builder notContains = new BooleanQuery.Builder();
                contains(notContains, targetField, value.get(), BooleanClause.Occur.MUST);
                queryBuilder.add(notContains.build(), BooleanClause.Occur.MUST_NOT);
            }
            default ->
                throw new UnsupportedOperationException("Unsupported operator: " + condition.operator());
        }
    }
    
        public static BooleanClause.Occur toOccur(LogicalOperator operator) {
        return switch (operator) {
            case AND ->
                BooleanClause.Occur.MUST;
            case OR ->
                BooleanClause.Occur.SHOULD;
        };
    }
}
