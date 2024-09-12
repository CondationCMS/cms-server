package com.github.thmarx.cms.api;

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


import com.github.thmarx.cms.api.media.MediaFormat;
import com.github.thmarx.cms.api.media.MediaUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ThemeProperties extends YamlProperties {

	public ThemeProperties(final Map<String, Object> properties) {
		super(properties);
	}

	public Double version() {
		return (Double)properties.get("version");
	}
	
	public String templateEngine() {
		return (String) getSubMap("template").get("engine");
	}

	public List<String> activeModules() {
		return (List<String>) getSubMap("modules").getOrDefault("active", List.of());
	}

	public Map<String, MediaFormat> getMediaFormats() {
		Map<String, MediaFormat> mediaFormats = new HashMap<>();
		Map<String, Object> media = (Map<String, Object>) properties.getOrDefault("media", Collections.emptyMap());
		List<Map<String, Object>> formats = (List<Map<String, Object>>) media.getOrDefault("formats", Collections.emptyList());
		formats.forEach(map -> {
			try {
				var mediaFormat = new MediaFormat(
						(String) map.get("name"),
						(int) map.get("width"),
						(int) map.get("height"),
						MediaUtils.format4String((String) map.get("format")),
						(boolean) map.get("compression"),
						(boolean) map.getOrDefault("cropped", false)
				);
				mediaFormats.put(mediaFormat.name(), mediaFormat);
			} catch (Exception e) {
				log.error("error createing format " + map.get("name"), e);
			}

		});

		return mediaFormats;
	}

}
