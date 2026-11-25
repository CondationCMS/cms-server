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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;

/**
 * Finds the package new extensions should be created in.
 */
public final class PackageDetector {

    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    private PackageDetector() {
    }

    /**
     * @return the most common package of the classes annotated with <code>@Extension(</code>, otherwise the
     *         (sanitized) group id
     */
    public static String detect(Path sourceDirectory, String groupId) {
        if (Files.isDirectory(sourceDirectory)) {
            try (var files = Files.walk(sourceDirectory)) {
                var counts = files
                        .filter(f -> f.getFileName().toString().endsWith(".java"))
                        .map(PackageDetector::extensionPackage)
                        .flatMap(java.util.Optional::stream)
                        .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
                var best = counts.entrySet().stream()
                        .max(Comparator.<java.util.Map.Entry<String, Long>>comparingLong(java.util.Map.Entry::getValue)
                                .thenComparing(java.util.Map.Entry::getKey, Comparator.reverseOrder()));
                if (best.isPresent()) {
                    return best.get().getKey();
                }
            } catch (IOException e) {
                // fall back to the group id
            }
        }
        return sanitize(groupId);
    }

    private static java.util.Optional<String> extensionPackage(Path file) {
        try {
            var content = Files.readString(file);
            if (!content.contains("@Extension(")) {
                return java.util.Optional.empty();
            }
            var matcher = PACKAGE.matcher(content);
            return matcher.find() ? java.util.Optional.of(matcher.group(1)) : java.util.Optional.empty();
        } catch (IOException e) {
            return java.util.Optional.empty();
        }
    }

    static String sanitize(String groupId) {
        return Arrays.stream(groupId.split("\\."))
                .map(segment -> segment.replaceAll("[^A-Za-z0-9_]", "_"))
                .map(segment -> segment.isEmpty() || Character.isDigit(segment.charAt(0)) ? "_" + segment : segment)
                .map(segment -> SourceVersion.isKeyword(segment) ? segment + "_" : segment)
                .collect(Collectors.joining("."));
    }
}
