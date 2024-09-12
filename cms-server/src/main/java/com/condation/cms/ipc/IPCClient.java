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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class IPCClient {

	private final IPCProperties properties;
	
	private final IPCCommands IPCCOMMANDS = new IPCCommands();

	public void send(Command command) throws Exception {
		try (Socket kkSocket = new Socket("localhost", properties.port()); 
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);) {
			
			if (properties.password().isPresent()) {
				command.setHeader("ipc.auth", properties.password().get());
			}
			
			out.write(IPCCOMMANDS.toJsonString(command));
		}
	}
	
	public static void main (String...args) throws Exception {
		new IPCClient(new IPCProperties(
				Map.of("port", 6868, "password", "test_pwd")
		)).send(new Command("shutdown"));
	}
}
