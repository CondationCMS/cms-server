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
import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class IPCServer extends Thread {

	private final IPCProperties properties;
	private final Consumer<Event> eventConsumer;
	boolean listening = true;
	
	@Override
	public void run() {

		try (ServerSocket serverSocket = new ServerSocket(properties.port())) {
			while (listening) {
				new IPCServerThread(serverSocket.accept(), eventConsumer, properties).start();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void stopListening () {
		listening = false;
	}
}
