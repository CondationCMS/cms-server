package com.github.thmarx.cms.cli.commands.server;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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
import com.github.thmarx.cms.auth.services.UserService;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "remove_user")
@Slf4j
public class RemoveUser implements Runnable {

	@CommandLine.Option(names = {"-r", "--realm"}, description = "The realm")
	String realm = "users";

	@CommandLine.Option(names = {"-h", "--host"}, description = "The host", required = true)
	String host = null;
	
	@Parameters(
			paramLabel = "<username>",
			index = "0",
			description = "The username."
	)
	private String username = "";

	@Override
	public void run() {
		try {
			UserService userService = new UserService(Path.of("hosts/" + host));
			
			userService.removeUser(UserService.Realm.of(realm), username);
			log.info("user added successfuly");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
