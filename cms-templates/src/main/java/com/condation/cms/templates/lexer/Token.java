package com.condation.cms.templates.lexer;

/*-
 * #%L
 * templates
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

public class Token {
    public static enum Type {
        TEXT, 
		VARIABLE_START,
		VARIABLE_END,
		COMPONENT_START,
		COMPONENT_END,
		TAG_START, 
		TAG_END, 
		COMMENT_START,
		COMMENT_END,
		COMMENT_VALUE,
		IDENTIFIER, 
		END, 
		EXPRESSION
    }

    public final Type type;
    public final String value;
	public final int line;
    public final int column;

    public Token(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
		this.line = line;
		this.column = column;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}
