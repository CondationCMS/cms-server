package com.condation.cms.templates.exceptions;

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

/**
 *
 * @author t.marx
 */
public class UnknownTagException extends RuntimeException {

	private final int line;
	private final int column;
	
	public UnknownTagException(String message, int line, int column) {
		super(message);
		this.line = line;
		this.column = column;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Error: %s (line %d, column %d)".formatted(getMessage(), line, column);
	}
	
	@Override
	public String getMessage() {
		return "Error: %s (line %d, column %d)".formatted(getMessage(), line, column);
	}
}
