package com.condation.cms.filesystem.metadata.query.parser.expressions;

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

import com.condation.cms.filesystem.metadata.query.parser.values.Value;
import java.util.List;
import java.util.stream.Collectors;

public record InCondition (String field, List<Value> values, boolean negated) implements Expression {
    @Override
    public String toString() {
        String op = negated ? "NOT IN" : "IN";
        return field + " " + op + " (" +
                values.stream().map(Object::toString).collect(Collectors.joining(", ")) +
                ")";
    }
}
