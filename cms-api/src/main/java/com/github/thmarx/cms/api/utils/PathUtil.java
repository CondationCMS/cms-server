package com.github.thmarx.cms.api.utils;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author thmar
 */
public class PathUtil {

	public static boolean isChild(Path possibleParent, Path maybeChild) throws IOException {
		return maybeChild.toFile().getCanonicalPath().startsWith(possibleParent.toFile().getCanonicalPath());
	}

	public static String toRelativePath(final Path contentPath, final Path contentBase) {
		Path tempPath = contentPath;
		if (!Files.isDirectory(contentPath)) {
			tempPath = contentPath.getParent();
		}
		Path relativize = contentBase.relativize(tempPath);
		var uri = relativize.toString();
		uri = uri.replaceAll("\\\\", "/");
		return uri;
	}

	public static String toRelativeFile(final Path contentFile, final Path contentBase) {
		Path relativize = contentBase.relativize(contentFile);
		if (Files.isDirectory(contentFile)) {
			relativize = relativize.resolve("index.md");
		}
		var uri = relativize.toString();
		uri = uri.replaceAll("\\\\", "/");
		return uri;
	}
	
	public static String toURI(final Path contentFile, final Path contentBase) {
		var relFile = toRelativeFile(contentFile, contentBase);
		if (relFile.endsWith("index.md")) {
			relFile = relFile.replace("index.md", "");
		}
		
		if (relFile.equals("")) {
			relFile = "/";
		} else if (relFile.endsWith("/")) {
			relFile = relFile.substring(0, relFile.lastIndexOf("/"));
		} 
		
		if (!relFile.startsWith("/")) {
			relFile = "/" + relFile;
		}
		if (relFile.endsWith(".md")) {
			relFile = relFile.substring(0, relFile.lastIndexOf(".md"));
		}
		
		
		return relFile;
	}
}
