package com.condation.cms.cli.commands.extensions;

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



import com.condation.cms.extensions.repository.RemoteRepository;
import com.google.common.base.Strings;
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
			paramLabel = "<host>",
			index = "1",
			description = "Host to install extension to."
	)
	private String host = "";
	
	@Override
	public void run() {
		
		if (Strings.isNullOrEmpty(extension)) {
			System.err.println("please provide extension name");
			return;
		}
		if (Strings.isNullOrEmpty(host)) {
			System.err.println("please provide install to install the extension");
			return;
		}
		
		if (repository.exists(extension)) {
			
			if (!isCompatibleWithServer(extension)) {
				throw new RuntimeException("the extension is not compatible with server version");
			}
			
			Optional<String> content = repository.getContent(extension);
			if (content.isEmpty()) {
				System.err.println("the extension content not found");
				return;
			}
			
			try {
				if (!Files.exists(Path.of("hosts/%s".formatted(host)))) {
					System.err.printf("site %s doesn't exists", host);
					return;
				}
				if (!Files.exists(Path.of("hosts/%s/extensions".formatted(host)))) {
					Files.createDirectories(Path.of("hosts/%s/extensions".formatted(host)));
				}
				
				Files.writeString(
						Path.of("hosts/%s/extensions/%s.js".formatted(host, extension)),
						content.get());
				
				System.out.printf("extension '%s' successfuly downloaded into '%s'!", extension, host);
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
	}
	
}
