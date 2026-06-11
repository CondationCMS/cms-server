package com.condation.cms.filesystem.metadata.query.parser.expressions;

import com.condation.cms.filesystem.metadata.query.Queries;

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


public enum Operator {
    EQ, NEQ, GT, GTE, LT, LTE, IN, NOT_IN, CONTAINS, CONTAINS_NOT;
    
	public static Operator forName (String name) {
		switch (name.toLowerCase()) {
			case "=": return EQ;
			case "!=": return NEQ;
			case ">": return GT;
			case ">=": return GTE;
			case "<": return LT;
			case "<=": return LTE;
		}
		for (var op : values()) {
			if (op.name().equalsIgnoreCase(name)) {
				return op;
			}
		}
		return null;
	}
    
    public Queries.Operator toQueriesOperator () {
        return switch (this) {
            case CONTAINS -> Queries.Operator.CONTAINS;
            case CONTAINS_NOT -> Queries.Operator.CONTAINS_NOT;
            case EQ -> Queries.Operator.EQ;
            case GT -> Queries.Operator.GT;
            case GTE -> Queries.Operator.GTE;
            case IN -> Queries.Operator.IN;
            case LT -> Queries.Operator.LT;
            case LTE -> Queries.Operator.LTE;
            case NEQ -> Queries.Operator.NOT_EQ;
            case NOT_IN -> Queries.Operator.NOT_IN;
        };
    }
}
