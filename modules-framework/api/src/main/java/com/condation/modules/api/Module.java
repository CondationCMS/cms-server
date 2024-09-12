package com.condation.modules.api;

/*-
 * #%L
 * modules-api
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


import java.util.List;

/**
 *
 * @author marx
 */
public interface Module {

	public enum Priority {
		NORMAL,
		// System modules must be loaded before default modules
		HIGH,
		HIGHER,
		HIGHEST;
	}
	
	Priority getPriority ();

	<T extends ExtensionPoint> List<T> extensions(Class<T> extensionClass);

	String getAuthor();

	String getDescription();

	String getId();

	String getName();

	String getVersion();

	boolean provides(Class<? extends ExtensionPoint> extensionClass);

}
