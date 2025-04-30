package com.condation.cms.modules.ui.utils;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2024 Marx-Software
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

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ContentFileParser {

	private String content;
	private Map<String, Object> header;

	public ContentFileParser(String filePath) throws IOException {
		String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
		parseFile(fileContent);
	}

	private void parseFile(String fileContent) {
		if (fileContent.startsWith("---")) {
			int endIndex = fileContent.indexOf("---", 3);
			if (endIndex != -1) {
				String headerContent = fileContent.substring(3, endIndex).trim();
				this.content = fileContent.substring(endIndex + 3).trim();
				parseHeader(headerContent);
			} else {
				this.content = fileContent;
			}
		} else {
			this.content = fileContent;
		}
	}

	private void parseHeader(String headerContent) {
		Yaml yaml = new Yaml();
		this.header = yaml.load(headerContent);
	}

	public String getContent() {
		return content;
	}

	public Map<String, Object> getHeader() {
		return header;
	}

}
