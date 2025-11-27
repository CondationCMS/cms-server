package com.condation.cms.core.mail;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 *
 * @author thorstenmarx
 */
public class MailConfigLoader {

	public static MailConfig load(Path configFile) {
		try {
			try (var configByteBuffer = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
				Yaml yaml = new Yaml(new Constructor(MailConfig.class, new LoaderOptions()));
				return yaml.load(configByteBuffer);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
