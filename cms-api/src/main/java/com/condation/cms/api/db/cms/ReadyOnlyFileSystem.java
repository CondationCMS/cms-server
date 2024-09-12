package com.condation.cms.api.db.cms;

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
 * The CMSFileSystem is a read only access abstraction to the underlying filesystem
 *
 * @author t.marx
 */
public interface ReadyOnlyFileSystem {

	/**
	 * Resolves a file if it is a child of the host base directory
	 * 
	 * @param path
	 * @return 
	 */
	ReadOnlyFile resolve (String path);
	
	/**
	 * creates a base directory for content.
	 * 
	 * @return 
	 */
	ReadOnlyFile contentBase ();
	
	/**
	 * creates a base directory for assets
	 * 
	 * @return 
	 */
	ReadOnlyFile assetBase ();
}
