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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class IPCServerThread extends Thread {

	private Socket socket = null;
	private Consumer<Event> eventConsumer;
	private IPCProtocol protocol;

	public IPCServerThread(Socket socket, Consumer<Event> eventConsumer, IPCProperties properties) {
		super("IPCServerThread");
		this.socket = socket;
		this.eventConsumer = eventConsumer;
		protocol = new IPCProtocol(eventConsumer, properties);
	}

	public void run() {

		try (
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(
						new InputStreamReader(
								socket.getInputStream()));) {
			String inputLine = in.readLine(); 
			
			protocol.processInput(inputLine.trim());
			
			
			socket.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
