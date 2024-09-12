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



import com.condation.cms.CMSServer;
import com.condation.cms.extensions.repository.RemoteRepository;
import lombok.Getter;

/**
 *
 * @author t.marx
 */
public abstract class AbstractExtensionCommand {

	@Getter
	private RemoteRepository repository = new RemoteRepository();

	public boolean isCompatibleWithServer(String extension) {
		var info = repository.getInfo(extension);
		if (info.isEmpty()) {
			throw new RuntimeException("extension not found");
		}
		
		return CMSServer.getVersion().satisfies(info.get().getCompatibility());
	}
}
