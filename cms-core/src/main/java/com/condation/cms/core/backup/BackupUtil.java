package com.condation.cms.core.backup;

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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class BackupUtil {

	/**
	 * Creates a tar.gz backup of the specified path while ignoring the temp/
	 * folder.
	 *
	 * @param sourceDir the path to the source directory
	 * @param targetFile the target file for the resulting tar.gz
	 * @throws IOException if any read or write errors occur
	 */
	public static void createTarGzBackup(Path sourceDir, Path targetFile) throws IOException {
		if (!Files.isDirectory(sourceDir)) {
			throw new IllegalArgumentException("sourceDir muss ein Ordner sein");
		}

		try (OutputStream fOut = Files.newOutputStream(targetFile); BufferedOutputStream buffOut = new BufferedOutputStream(fOut); GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut); TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzOut)) {

			tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

			Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					// temp/ Ordner ignorieren
					if (dir.getFileName().toString().equalsIgnoreCase("temp")) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					if (!sourceDir.equals(dir)) {
						// Relativer Pfad
						Path relativePath = sourceDir.relativize(dir);
						TarArchiveEntry entry = new TarArchiveEntry(dir.toFile(), relativePath.toString() + "/");
						tarOut.putArchiveEntry(entry);
						tarOut.closeArchiveEntry();
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					// Datei schreiben
					Path relativePath = sourceDir.relativize(file);
					TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), relativePath.toString());
					entry.setSize(Files.size(file));
					tarOut.putArchiveEntry(entry);
					Files.copy(file, tarOut);
					tarOut.closeArchiveEntry();
					return FileVisitResult.CONTINUE;
				}
			});

			tarOut.finish();
		}
	}
}
