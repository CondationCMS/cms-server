package com.github.thmarx.cms.modules.ui.services;

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