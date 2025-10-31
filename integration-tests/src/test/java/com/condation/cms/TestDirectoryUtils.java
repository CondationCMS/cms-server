package com.condation.cms;

/*-
 * #%L
 * integration-tests
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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class TestDirectoryUtils {

    /**
     * Kopiert den Inhalt eines Verzeichnisses rekursiv in ein anderes Verzeichnis.
     *
     * @param sourceDir Quellverzeichnis (muss existieren)
     * @param targetDir Zielverzeichnis (wird erstellt, falls nicht vorhanden)
     * @throws IOException wenn beim Kopieren ein Fehler auftritt
     */
    public static void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(sourceDir)) {
            throw new IllegalArgumentException("Source directory does not exist: " + sourceDir);
        }

        if (Files.exists(targetDir)) {
            // Optional: löschen, um sicherzugehen, dass keine alten Files stören
            deleteDirectory(targetDir);
        }

        Files.createDirectories(targetDir);

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
                Files.createDirectories(targetPath);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(file));
                Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Löscht ein Verzeichnis rekursiv.
     *
     * @param dir Verzeichnis, das gelöscht werden soll
     * @throws IOException wenn ein Fehler auftritt
     */
    public static void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
                Files.deleteIfExists(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
