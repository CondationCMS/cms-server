package com.github.thmarx.cms.api;

/*-
 * #%L
 * cms-server
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public class ThemeProperties extends YamlProperties {
	
	public ThemeProperties (final Map<String, Object> properties) {
		super(properties);
	}
	
	public String templateEngine () {
		return (String)getSubMap("template").get("engine");
	}
	
	public List<String> activeModules () {
		return (List<String>)getSubMap("modules").getOrDefault("active", List.of());
	}
	
	public Map<String, MediaFormat> getMediaFormats() {
			Map<String, MediaFormat> mediaFormats = new HashMap<>();
			Map<String, Object> media = (Map<String, Object>) properties.getOrDefault("media", Collections.emptyMap());
			List<Map<String, Object>> formats = (List<Map<String, Object>>) media.getOrDefault("formats", Collections.emptyList());
			formats.forEach(map -> {
				var mediaFormat = new MediaFormat(
						(String) map.get("name"),
						(int) map.get("width"),
						(int) map.get("height"),
						Media.format4String((String) map.get("format")),
						(boolean) map.get("compression")
				);
				mediaFormats.put(mediaFormat.name(), mediaFormat);
			});

		return mediaFormats;
	}
	
	public static record MediaFormat(String name, int width, int height, Media.Format format, boolean compression) {
	}
}
