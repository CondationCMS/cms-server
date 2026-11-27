package com.condation.cms.maven.add.types;

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

import com.condation.cms.maven.add.ExtensionSpec;
import com.condation.cms.maven.add.ExtensionType;
import com.condation.cms.maven.add.Names;
import com.condation.cms.maven.add.Question;
import java.util.List;

/**
 * Common parts of the extension types: metadata and a tiny template mechanism.
 * Placeholders are written as <code>{{key}}</code>; values for string literals are escaped by the type.
 */
abstract class BaseType implements ExtensionType {

    private final String id;
    private final String label;
    private final List<Question> questions;

    BaseType(String id, String label, Question... questions) {
        this.id = id;
        this.label = label;
        this.questions = List.of(questions);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public List<Question> questions() {
        return questions;
    }

    /** Escaped value of an option for use inside a Java string literal. */
    static String literal(ExtensionSpec spec, String key) {
        return Names.javaString(spec.option(key));
    }

    /** Escaped value of a path option that must start with a slash. */
    static String absolutePath(ExtensionSpec spec, String key) {
        return Names.javaString(Names.requireAbsolutePath(spec.option(key)));
    }

    /** Escaped value of a relative path option. */
    static String relativePath(ExtensionSpec spec, String key) {
        return Names.javaString(Names.requireRelativePath(spec.option(key)));
    }

    /** Value of an option that must be one of the allowed values. */
    static String choice(ExtensionSpec spec, String key, String... allowed) {
        var value = spec.option(key);
        if (!java.util.Arrays.asList(allowed).contains(value)) {
            throw new IllegalArgumentException("Invalid value for '" + key + "': " + value
                    + " (allowed: " + String.join(", ", allowed) + ")");
        }
        return value;
    }

    /** Value of an option that becomes a Java identifier (validated). */
    static String identifier(ExtensionSpec spec, String key) {
        return Names.requireIdentifier(spec.option(key));
    }

    /**
     * Replaces <code>{{package}}</code> and <code>{{class}}</code> and every given key/value pair.
     */
    static String fill(ExtensionSpec spec, String template, String... keyValues) {
        var result = template
                .replace("{{package}}", spec.packageName())
                .replace("{{class}}", spec.className());
        for (int i = 0; i < keyValues.length; i += 2) {
            result = result.replace("{{" + keyValues[i] + "}}", keyValues[i + 1]);
        }
        return result;
    }
}
