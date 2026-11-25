package com.condation.cms.maven.add;

/*-
 * #%L
 * CondationCMS Maven Plugin
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

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;

/**
 * Validation and escaping helpers for generated source code.
 */
public final class Names {

    private Names() {
    }

    public static String requireClassName(String value) {
        return requireIdentifier(value, "class name");
    }

    public static String requireIdentifier(String value) {
        return requireIdentifier(value, "name");
    }

    public static String requirePackage(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Package must not be empty");
        }
        if (!SourceVersion.isName(value)) {
            throw new IllegalArgumentException("Invalid package: '" + value + "'");
        }
        return value;
    }

    private static String requireIdentifier(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The " + what + " must not be empty");
        }
        if (!isIdentifier(value)) {
            throw new IllegalArgumentException("Invalid " + what + ": '" + value + "' (must be a valid Java identifier)");
        }
        return value;
    }

    public static boolean isIdentifier(String value) {
        return value != null && SourceVersion.isIdentifier(value) && !SourceVersion.isKeyword(value);
    }

    public static boolean isAbsolutePath(String value) {
        return value != null && value.startsWith("/") && value.chars().noneMatch(c -> Character.isWhitespace(c) || c == '\\');
    }

    public static boolean isRelativePath(String value) {
        return value != null && !value.isBlank() && !value.startsWith("/")
                && value.chars().noneMatch(c -> Character.isWhitespace(c) || c == '\\' || c == '?')
                && java.util.Arrays.stream(value.split("/", -1)).noneMatch(".."::equals);
    }

    /** A path as used by http handlers and routes: starts with a slash, e.g. <code>/my-api</code>. */
    public static String requireAbsolutePath(String value) {
        if (!isAbsolutePath(value)) {
            throw new IllegalArgumentException("Invalid path '" + value + "': must start with / and contain no spaces (e.g. /my-api)");
        }
        return value;
    }

    /** A path as used by realtime endpoints: relative, e.g. <code>chat/room</code>. */
    public static String requireRelativePath(String value) {
        if (!isRelativePath(value)) {
            throw new IllegalArgumentException("Invalid path '" + value
                    + "': must be relative (no leading /, no .., ?, spaces or backslash), e.g. chat/room");
        }
        return value;
    }

    /**
     * Escapes a value for the inside of a Java string literal (without the quotes).
     */
    public static String javaString(String value) {
        var sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * <code>template-function</code> becomes <code>MyTemplateFunctionExtension</code>.
     */
    public static String defaultClassName(ExtensionType type) {
        var camel = Arrays.stream(type.id().split("-"))
                .filter(p -> !p.isEmpty())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
                .collect(Collectors.joining());
        return "My" + camel + "Extension";
    }

}
