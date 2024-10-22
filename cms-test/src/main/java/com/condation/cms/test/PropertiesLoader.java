package com.condation.cms.test;

/*-
 * #%L
 * cms-api
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


import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.ThemeProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
public abstract class PropertiesLoader {
	
	public static Map<String, Object> rawProperties (Path path) throws IOException {
		return new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
	}
	
	public static SiteProperties hostProperties (Path path) throws IOException {
		Map<String, Object> properties = Map.of();
		if (Files.exists(path)) {
			properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		}
		return new TestSiteProperties(properties);
	}
	
	public static ThemeProperties themeProperties (Path path) throws IOException {
		Map<String, Object> properties = Map.of();
		if (Files.exists(path)) {
			properties = new Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
		}
		return new TestThemeProperties(properties);
	}
}
