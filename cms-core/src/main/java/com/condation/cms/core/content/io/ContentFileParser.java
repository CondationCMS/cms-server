package com.condation.cms.core.content.io;

/*-
 * #%L
 * CMS Core
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.db.cms.ReadOnlyFile;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ContentFileParser {

	private String content;
	private Map<String, Object> header = new HashMap<>();

	public ContentFileParser(String filePath) throws IOException {
		String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
		parseFile(fileContent);
	}
	
	public ContentFileParser (ReadOnlyFile contentFile) throws IOException {
		parseFile(contentFile.getContent());
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
		Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
		Map<String, Object> parsedHeader = yaml.load(headerContent);
		this.header = parsedHeader == null ? new HashMap<>() : parsedHeader;
	}

	public String getContent() {
		return content;
	}

	public Map<String, Object> getHeader() {
		return header;
	}

}
