package com.github.thmarx.cms.api.utils;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author thmar
 */
public class PathUtil {

	public static boolean isContentFile (final Path file) {
		return file.toString().endsWith(".md");
	}
	
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
