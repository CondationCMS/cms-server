package com.condation.cms.modules.ui.model;

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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;


/**
 *
 * @author marx
 */
@Slf4j
public class User {

	private String id;
	private String username;
	private String password;
	private final SortedSet<String> groups = new ConcurrentSkipListSet<>();

	final static Splitter userSplitter = Splitter.on(":").trimResults();
	final static Splitter groupSplitter = Splitter.on(",").trimResults();
	final static Joiner userJoiner = Joiner.on(":");
	final static Joiner groupJoiner = Joiner.on(",");

	public User() {
	}

	public String id() {
		return id;
	}

	public User id(String id) {
		this.id = id;
		return this;
	}
	
	public String username() {
		return username;
	}

	public User username(String username) {
		this.username = username;
		return this;
	}

	public String password() {
		return password;
	}

	public User password(String password) {
		this.password = password;
		return this;
	}

	public Set<String> groups() {
		return groups;
	}

	public User group(String group) {
		groups.add(group);
		return this;
	}

	/**
	 * String format: 
	 * <id>:<username>:<password.hash>:group,group
	 *
	 * @param userString
	 * @return
	 */
	public static User fromString(final String userString) {
		User user = new User();

		List<String> userParts = userSplitter.splitToList(userString);

		user.id(userParts.get(0));
		user.username(userParts.get(1));
		user.password(userParts.get(2));
		groupSplitter.split(userParts.get(3)).forEach((group) -> {
			user.group(group);
		});

		return user;
	}

	@Override
	public String toString() {
		//<id>:<username>:<password.hash>:group,group
		StringBuilder sb = new StringBuilder();

		sb.append(userJoiner.join(id(), username(), password(), groupJoiner.join(groups)));

		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}

}
