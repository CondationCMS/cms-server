package com.github.thmarx.cms.modules.ui.services;

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

import com.github.thmarx.cms.modules.ui.model.Command;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author t.marx
 */
public class CommandService {
	
	private Gson GSON = new Gson();
	
	public Map<String, CommandHandler> handlers = new HashMap<>();
	
	public void register (final String type, CommandHandler handler) {
		handlers.put(type, handler);
	}
	
	public Optional<?> execute (final String body) {
		var command = GSON.fromJson(body, Command.class);
		
		if (!handlers.containsKey(command.type())) {
			return Optional.empty();
		} 
		return Optional.ofNullable(handlers.get(command.type()).execute(command));
	}
	
	public Optional<?> execute (final Command command) {
		if (!handlers.containsKey(command.type())) {
			return Optional.empty();
		} 
		return Optional.ofNullable(handlers.get(command.type()).execute(command));
	}
	
	public static interface CommandHandler {
		public Object execute (final Command command);
	}
}
