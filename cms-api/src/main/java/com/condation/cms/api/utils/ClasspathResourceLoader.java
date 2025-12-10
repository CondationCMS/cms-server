package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility class for loading resources from the classpath as String.
 */
public final class ClasspathResourceLoader {

    private ClasspathResourceLoader() {
        // Utility class: no instances allowed
    }

    /**
     * Loads a resource relative to the given class and returns its content as a String.
     *
     * @param baseClass    class whose package is used as base for resolving the path
     * @param relativePath relative path, e.g. "data/test.txt" or "/config/app.yml"
     * @return content of the resource as String
     * @throws IllegalArgumentException  if parameters are invalid or the resource cannot be found
     * @throws UncheckedIOException      if an I/O error occurs while reading
     */
    public static String loadRelative(Class<?> baseClass, String relativePath) {
        Objects.requireNonNull(baseClass, "baseClass must not be null");
        Objects.requireNonNull(relativePath, "relativePath must not be null");

        try (InputStream in = baseClass.getResourceAsStream(relativePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found relative to "
                        + baseClass.getName() + ": " + relativePath);
            }
            return toString(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource relative to "
                    + baseClass.getName() + ": " + relativePath, e);
        }
    }

    /**
     * Loads a resource using an absolute classpath path and returns its content as a String.
     *
     * @param absolutePath absolute path on the classpath, e.g. "config/app.yml" or "data/test.txt"
     * @return content of the resource as String
     * @throws IllegalArgumentException  if the path is invalid or the resource cannot be found
     * @throws UncheckedIOException      if an I/O error occurs while reading
     */
    public static String loadAbsolute(String absolutePath) {
        Objects.requireNonNull(absolutePath, "absolutePath must not be null");

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClasspathResourceLoader.class.getClassLoader();
        }

        try (InputStream in = cl.getResourceAsStream(absolutePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found on classpath: " + absolutePath);
            }
            return toString(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource from classpath: " + absolutePath, e);
        }
    }

    /**
     * Reads an InputStream completely as a UTF-8 String.
     *
     * @param in input stream to read from
     * @return content of the stream as String
     * @throws IOException if an I/O error occurs while reading
     */
    private static String toString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
        }
        return sb.toString();
    }
}
