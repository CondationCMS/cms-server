package com.condation.cms.api.utils;

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
import com.condation.cms.api.db.cms.ReadOnlyFile;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
public class PathUtil {

	public static String canonicalPath(final Path file) {
		return file.toAbsolutePath().normalize().toString();
	}

	public static boolean isContentFile(final Path file) {
		return file.toString().endsWith(".md");
	}

	public static boolean isChild(Path possibleParent, Path maybeChild) {
		if (maybeChild == null || possibleParent == null) {
			return false;
		}
		return canonicalPath(maybeChild).startsWith(canonicalPath(possibleParent));
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

	public static String toRelativePath(final ReadOnlyFile contentPath, final ReadOnlyFile contentBase) {
		ReadOnlyFile tempPath = contentPath;
		if (!contentPath.isDirectory()) {
			tempPath = contentPath.getParent();
		}
		ReadOnlyFile relativize = contentBase.relativize(tempPath);
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

	public static String toRelativeFile(ReadOnlyFile contentFile, final ReadOnlyFile contentBase) {
		if (contentFile.isDirectory()) {
			contentFile = contentFile.resolve("index.md");
		}
		var relativize = contentBase.relativize(contentFile);
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

	public static String toURI(final ReadOnlyFile contentFile, final ReadOnlyFile contentBase) {
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
