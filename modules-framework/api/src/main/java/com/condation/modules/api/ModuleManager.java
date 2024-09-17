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




import java.io.IOException;
import java.util.List;

/**
 *
 * @author marx
 */
public interface ModuleManager extends AutoCloseable {

	/**
	 * activates a module.
	 *
	 * @param moduleId
	 * @return returns true if the module is correctly or allready installed, otherwise false
	 * @throws java.io.IOException
	 */
	boolean activateModule(final String moduleId) throws IOException;
	
	/**
	 *
	 * @param moduleId
	 * @return
	 */
	boolean deactivateModule(final String moduleId) throws IOException;

	void initModules();
	
	/**
	 * Returns the module description.
	 * @param id
	 * @return
	 * @throws IOException
	 */
	ModuleDescription description(final String id) throws IOException;

	/**
	 * Returns all Extensions of the given type.
	 *
	 * @param <T>
	 * @param extensionClass
	 * @return
	 */
	<T extends ExtensionPoint> List<T> extensions(Class<T> extensionClass);

	public Module module(final String id);
	
	/**
	 * Returns the configuration of the module manager.
	 *
	 * @return
	 */
	public ManagerConfiguration configuration();
	
	/**
	 * Returns a list of all available module ids.
	 * 
	 * @return 
	 */
	public List<String> getModuleIds ();
}
