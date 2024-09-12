package com.condation.cms.ipc;

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



import com.google.gson.Gson;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class IPCCommands {

	public static final Gson GSON = new Gson();
	
	public Optional<Command> parse (String commandJson) {
		try {
			return Optional.of(GSON.fromJson(commandJson, Command.class));
		} catch (Exception e) {
			log.error("", e);
		}
		return Optional.empty();
	}
	
	public String toJsonString (Command command) {
		return GSON.toJson(command);
	}
}
