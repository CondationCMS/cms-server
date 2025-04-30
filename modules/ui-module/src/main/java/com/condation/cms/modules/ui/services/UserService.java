package com.condation.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
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

import com.condation.cms.modules.ui.model.User;
import java.util.Collection;

/**
 *
 * @author marx
 */
public interface UserService {
    /**
     * Adds an user to the store, if the username already exists, the user will be updated
     * @param user 
     */
    public void add (User user);
    /**
	 * 
	 * @param username
	 * @return
	 */
    public User get (String username);
    /**
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
    public User login (String username, String password);
	
	/**
	 * deletes a user.
	 * 
	 * @param user
	 */
	public void delete (User user);
	/**
	 * returns all users.
	 * @return
	 */
	public Collection<User> all ();
}
