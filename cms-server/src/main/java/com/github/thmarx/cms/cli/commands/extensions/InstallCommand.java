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

import com.github.thmarx.cms.extensions.repository.RemoteRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 *
 * @author t.marx
 */
@Slf4j
@CommandLine.Command(name = "install")
public class InstallCommand extends AbstractExtensionCommand implements Runnable {

	RemoteRepository repository = new RemoteRepository();
	
	@CommandLine.Parameters(
			paramLabel = "<extension>",
			index = "0",
			description = "The id of the extension."
	)
	private String extension = "";
	
	@CommandLine.Parameters(
			paramLabel = "<site>",
			index = "1",
			description = "Site to install extension to."
	)
	private String site = "";
	
	@Override
	public void run() {
		System.out.println("install extension: " + extension);
		System.out.println("module: " + repository.exists(extension));
		if (repository.exists(extension)) {
			
			if (!isCompatibleWithServer(extension)) {
				throw new RuntimeException("the extension is not compatible with server version");
			}
			
			Optional<String> content = repository.getContent(extension);
			if (content.isEmpty()) {
				throw new RuntimeException("the extension content not found");
			}
			
			try {
				if (!Files.exists(Path.of("hosts/%s".formatted(site)))) {
					throw new RuntimeException("site %s doesn't exists".formatted(site));
				}
				if (!Files.exists(Path.of("hosts/%s/extensions".formatted(site)))) {
					Files.createDirectories(Path.of("hosts/%s/extensions".formatted(site)));
				}
				
				Files.writeString(
						Path.of("hosts/%s/extensions/%s.js".formatted(site, extension)),
						content.get());
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
	}
	
}
