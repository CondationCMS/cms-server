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

import java.util.List;
import java.util.function.Predicate;

/**
 * A type specific question.
 *
 * @param key          option key, also used in <code>condation.add.options</code>
 * @param label        text shown to the user
 * @param defaultValue value used when the user just presses enter
 * @param choices      allowed values, empty for free text
 * @param validator    accepts valid free text answers
 * @param errorMessage shown when the validator rejects an answer
 */
public record Question(String key, String label, String defaultValue, List<String> choices,
        Predicate<String> validator, String errorMessage) {

    private static final Predicate<String> NOT_BLANK = s -> s != null && !s.isBlank();

    public static Question text(String key, String label, String defaultValue) {
        return new Question(key, label, defaultValue, List.of(), NOT_BLANK, "Must not be empty");
    }

    /** A Java identifier, e.g. a parameter name. */
    public static Question identifier(String key, String label, String defaultValue) {
        return new Question(key, label, defaultValue, List.of(), Names::isIdentifier,
                "Must be a valid Java identifier (letters, digits, _; no spaces, dashes or keywords)");
    }

    /** A path starting with a slash, e.g. <code>/my-api</code>. */
    public static Question absolutePath(String key, String label, String defaultValue) {
        return new Question(key, label, defaultValue, List.of(), Names::isAbsolutePath,
                "Must start with / and contain no spaces, e.g. /my-api");
    }

    /** A relative path, e.g. <code>chat/room</code>. */
    public static Question relativePath(String key, String label, String defaultValue) {
        return new Question(key, label, defaultValue, List.of(), Names::isRelativePath,
                "Must be relative: no leading /, no .., ?, spaces or backslash, e.g. chat/room");
    }

    public static Question choice(String key, String label, String defaultValue, String... choices) {
        return new Question(key, label, defaultValue, List.of(choices), NOT_BLANK, "Must not be empty");
    }
}
