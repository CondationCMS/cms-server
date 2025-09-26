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

public enum ComparisonOperator {
    EQ("="),
    NE("!="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    IN("IN"),
    NOT_IN("NOT IN"),
    CONTAINS("CONTAINS"),
    CONTAINS_NOT("CONTAINS NOT");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    public static ComparisonOperator fromToken(String token) {
        return switch (token.toUpperCase()) {
            case "=" -> EQ;
            case "!=" -> NE;
            case "<" -> LT;
            case "<=" -> LE;
            case ">" -> GT;
            case ">=" -> GE;
            case "IN" -> IN;
            case "NOT IN" -> NOT_IN;
            case "CONTAINS" -> CONTAINS;
            case "CONTAINS NOT" -> CONTAINS_NOT;
            default -> throw new IllegalArgumentException("Unknown operator: " + token);
        };
    }
}
