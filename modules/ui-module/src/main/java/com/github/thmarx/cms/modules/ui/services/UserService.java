package com.github.thmarx.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.modules.ui.model.User;
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
