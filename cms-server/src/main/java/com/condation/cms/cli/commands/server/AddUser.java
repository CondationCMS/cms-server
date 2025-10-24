package com.condation.cms.cli.commands.server;

import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.ServerUtil;
import com.condation.cms.auth.services.Realm;

/*-
 * #%L
 * cms-server
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


import com.condation.cms.auth.services.UserService;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(
		name = "add_user",
		description = {
			"adds a user to a realm"
		}
)
@Slf4j
public class AddUser implements Runnable {

	@CommandLine.Option(names = {"-r", "--realm"}, description = "The realm")
	String realm = "users";
	
	@CommandLine.Option(names = {"-ro", "--roles"}, description = "The roles", split = ",")
	String[] roles = null;
	
	@Parameters(
			paramLabel = "<username>",
			index = "0",
			description = "The username."
	)
	private String username = "";
	
	@Parameters(
			paramLabel = "<password>",
			index = "1",
			description = "The username."
	)
	private String password = "";

	@Parameters(
			paramLabel = "<mail>",
			index = "2",
			description = "The users mail address."
	)
	private String mail = "";
	
	@Override
	public void run() {
		try {
			UserService userService = new UserService(ServerUtil.getHome());
			
			Map<String, Object> data = new HashMap<>();
			if (!Strings.isNullOrEmpty(mail)) {
				data.put("mail", mail);
			}
			userService.addUser(Realm.of(realm), username, password, roles, data);
			log.info("user added successfuly");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
