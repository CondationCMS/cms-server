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



import com.condation.cms.api.IPCProperties;
import com.condation.cms.api.eventbus.Event;
import com.condation.cms.api.eventbus.events.RepoCheckoutEvent;
import com.condation.cms.api.eventbus.events.lifecycle.ReloadHostEvent;
import com.condation.cms.api.eventbus.events.lifecycle.ServerShutdownInitiated;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class IPCProtocol {

	private final Consumer<Event> eventConsumer;
	
	private final IPCProperties properties;
	
	private final IPCCommands ipcCommands = new IPCCommands();

	public void processInput(final String theInput) {
		
		var commandOpt = ipcCommands.parse(theInput);
		if (commandOpt.isEmpty()) {
			return;
		}
		var command = commandOpt.get();
		if (properties.password().isPresent()) {
			if (command.getHeader("ipc.auth").isEmpty()) {
				log.warn("no ipc.auth header set");
				return;
			} else if (!command.getHeader("ipc.auth").get().equals(properties.password().get())) {
				log.warn("unauthorized ipc call");
				return;
			}
		}
				
		
		if ("shutdown".equals(command.getCommand())) {
			eventConsumer.accept(new ServerShutdownInitiated());
		} else if ("reload_host".equals(command.getCommand())) {
			var hostHeader = command.getHeader("host");
			if (!hostHeader.isPresent()) {
				log.warn("host header not set");
				return;
			}
			log.debug("trigger reload host event");
			eventConsumer.accept(new ReloadHostEvent((String)hostHeader.get()));
		} else if ("repo_checkout".equals(command.getCommand())) {
			var repoHeader = command.getHeader("repo");
			if (!repoHeader.isPresent()) {
				log.warn("repo header not set");
				return;
			}
			log.debug("trigger checkout of repo {}", repoHeader);
			eventConsumer.accept(new RepoCheckoutEvent((String)repoHeader.get()));
		}
	}
}
