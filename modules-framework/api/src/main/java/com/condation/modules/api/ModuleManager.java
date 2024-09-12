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




import com.condation.modules.api.Module;
import java.io.IOException;
import java.net.URI;
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

	/**
	 * install a new module.
	 *
	 * @param moduleURI
	 * @return the id of the newly installed module.
	 * @throws IOException
	 */
	String installModule(final URI moduleURI) throws IOException;

	/**
	 * uninstall module,
	 *
	 * @param moduleId the ID of the module
	 * @param deleteData should the data directory of the module be deleted too.
	 * @return
	 * @throws IOException
	 */
	boolean uninstallModule(final String moduleId, final boolean deleteData) throws IOException;
	
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
