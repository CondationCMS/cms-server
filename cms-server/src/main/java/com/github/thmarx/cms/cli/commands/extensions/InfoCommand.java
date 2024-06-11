package com.github.thmarx.cms.cli.commands.extensions;

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
import com.github.thmarx.cms.CMSServer;
import com.github.thmarx.cms.extensions.repository.ExtensionInfo;
import com.github.thmarx.cms.extensions.repository.RemoteRepository;
import com.google.common.base.Strings;
import java.util.Optional;
import lombok.Setter;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@CommandLine.Command(name = "info")
public class InfoCommand implements Runnable {

	RemoteRepository repository = new RemoteRepository();

	@CommandLine.Parameters(
			paramLabel = "<extension>",
			index = "0",
			description = "The id of the extension."
	)
	@Setter
	private String extension = "";

	@Override
	public void run() {
		if (Strings.isNullOrEmpty(extension)) {
			System.err.println("please provide extension name");
			return;
		}
		if (!repository.exists(extension)) {
			System.err.printf("extension %s not found\r\n", extension);
			return;
		}
		
		final Optional<ExtensionInfo> extInfo = repository.getInfo(extension);
		var info = extInfo.get();

		System.out.println("extension: " + info.getId());
		System.out.println("name: " + info.getName());
		System.out.println("description: " + info.getDescription());
		System.out.println("author: " + info.getAuthor());
		System.out.println("url: " + info.getUrl());
		System.out.println("compatibility: " + info.getCompatibility());
		System.out.println("your server version: " + CMSServer.getVersion().getVersion());
		System.out.println("compatibility with server version: " + CMSServer.getVersion().satisfies(info.getCompatibility()));
	}
}
