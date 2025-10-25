package com.condation.cms.filesystem.metadata.query.parser;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import com.condation.cms.filesystem.metadata.query.parser.expressions.Expression;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Condition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.ContainsCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Logical;
import com.condation.cms.filesystem.metadata.query.parser.expressions.InCondition;
import com.condation.cms.filesystem.metadata.query.parser.expressions.LogicalOperator;
import com.condation.cms.filesystem.metadata.query.parser.expressions.Operator;
import com.condation.cms.filesystem.metadata.query.parser.values.NumberValue;
import com.condation.cms.filesystem.metadata.query.parser.values.BooleanValue;
import com.condation.cms.filesystem.metadata.query.parser.values.Value;
import com.condation.cms.filesystem.metadata.query.parser.values.StringValue;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static class Context {
        private final List<Token> tokens;
        private int pos = 0;

        Context(List<Token> tokens) {
            this.tokens = tokens;
        }

        Token current() {
            return tokens.get(pos);
        }

        boolean match(String token) {
            if (pos < tokens.size() && tokens.get(pos).text().equalsIgnoreCase(token)) {
                pos++;
                return true;
            }
            return false;
        }

        Token consume() {
            if (pos >= tokens.size()) {
                throw new RuntimeException("Unexpected end");
            }
            return tokens.get(pos++);
        }

        boolean hasNext() {
            return pos < tokens.size();
        }
    }

    public Expression parse(String input) {
        var context = new Context(Tokenizer.tokenize(input));
        Expression expr = parseOr(context);
        if (context.current().type() != TokenType.EOF) {
		   throw new RuntimeException("Unexpected token: " + context.current());
		}
        return expr;
    }

    private Expression parseOr(Context context) {
        Expression expr = parseAnd(context);
        while (context.match("OR")) {
            Expression right = parseAnd(context);
            expr = new Logical(expr, LogicalOperator.OR, right);
        }
        return expr;
    }

    private Expression parseAnd(Context context) {
        Expression expr = parsePrimary(context);
        while (context.match("AND")) {
            Expression right = parsePrimary(context);
            expr = new Logical(expr, LogicalOperator.AND, right);
        }
        return expr;
    }

    private Expression parsePrimary(Context context) {
        if (context.match("(")) {
            Expression expr = parseOr(context);
            if (!context.match(")")) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return expr;
        }
        return parseComparison(context);
    }

    private Expression parseComparison(Context context) {
        String field = context.consume().text();
        String op = context.consume().text().toUpperCase();

        switch (op) {
            case "IN":
                return parseInCondition(context, field, false);
            case "NOT IN":
                return parseInCondition(context, field, true);
            case "CONTAINS":
                return parseContainsCondition(context, field, false);
            case "CONTAINS NOT":
                return parseContainsCondition(context, field, true);
            default:
                // alle Standardoperatoren (=, !=, <, <=, >, >=)
                return new Condition(field, Operator.forName(op), parseValue(context.consume().text()));
        }
    }

	private ContainsCondition parseContainsCondition(Context context, String field, boolean negated) {
        if (!context.match("(")) {
            throw new RuntimeException("Expected '(' after CONTAINS/CONTAINS NOT");
        }
        List<Value> values = new ArrayList<>();
        do {
            values.add(parseValue(context.consume().text()));
        } while (context.match(","));
        if (!context.match(")")) {
            throw new RuntimeException("Missing closing ')' in CONTAINS/CONTAINS NOT");
        }
        return new ContainsCondition(field, values, negated);
    }
	
    private InCondition parseInCondition(Context context, String field, boolean negated) {
        if (!context.match("(")) {
            throw new RuntimeException("Expected '(' after IN/NOT IN");
        }
        List<Value> values = new ArrayList<>();
        do {
            values.add(parseValue(context.consume().text()));
        } while (context.match(","));
        if (!context.match(")")) {
            throw new RuntimeException("Missing closing ')' in IN/NOT IN");
        }
        return new InCondition(field, values, negated);
    }

    private Value parseValue(String token) {
        String lower = token.toLowerCase();
        if (lower.equals("true") || lower.equals("false")) {
            return new BooleanValue(Boolean.valueOf(lower));
        }
        try {
            if (token.contains(".")) {
                return new NumberValue(Double.valueOf(token));
            } else {
                return new NumberValue(Integer.valueOf(token));
            }
        } catch (NumberFormatException e) {
            return new StringValue(token);
        }
    }
}
