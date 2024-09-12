package com.github.thmarx.cms.api.exceptions;

/*-
 * #%L
 * cms-api
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
public class IllegalConfigurationException extends RuntimeException {

	/**
	 * Creates a new instance of <code>IllegalConfigurationException</code> without detail message.
	 */
	public IllegalConfigurationException() {
	}

	/**
	 * Constructs an instance of <code>IllegalConfigurationException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public IllegalConfigurationException(String msg) {
		super(msg);
	}
	
	public IllegalConfigurationException(String msg, Throwable throwable) {
		super(msg, throwable);
	}
}
