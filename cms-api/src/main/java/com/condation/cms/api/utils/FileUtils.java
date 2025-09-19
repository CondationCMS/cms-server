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
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

/**
 *
 * @author thmar
 */
public class FileUtils {

	private FileUtils() {
	}

	public static void deleteFolder(Path pathToBeDeleted) throws IOException {
		if (!Files.exists(pathToBeDeleted)) {
			return;
		}
		try (var walkStream = Files.walk(pathToBeDeleted)) {
			walkStream.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
	}

	public static void deleteDirectoryContents(Path directory) throws IOException {
		if (!Files.exists(directory) || !Files.isDirectory(directory)) {
			throw new IllegalArgumentException("Pfad ist kein existierendes Verzeichnis: " + directory);
		}

		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (!dir.equals(directory)) {
					Files.delete(dir);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void touch(Path path) throws IOException {
		long timeMillis = System.currentTimeMillis();
		FileTime accessFileTime = FileTime.fromMillis(timeMillis);
		Files.setAttribute(path, "lastAccessTime", accessFileTime);
		Files.setLastModifiedTime(path, accessFileTime);
	}

	public static long countChildren(final Path path) throws IOException {
		try (var children = Files.list(path)) {
			return children.count();
		}
	}
}
