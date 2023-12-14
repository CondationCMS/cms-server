package com.github.thmarx.cms.git;

/*-
 * #%L
 * cms-git
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public class Config {

	public static Config load (final Path file) throws IOException {
		Yaml yaml = new Yaml();
		return yaml.loadAs(Files.readString(file), Config.class);
	}
	
	@Getter
	@Setter
	List<Repo> repos;
	
	
}
