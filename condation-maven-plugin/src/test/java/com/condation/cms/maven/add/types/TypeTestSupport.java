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

import com.condation.cms.maven.add.CompileHelper;
import com.condation.cms.maven.add.ExtensionSpec;
import com.condation.cms.maven.add.ExtensionType;
import com.condation.cms.maven.add.ExtensionTypes;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared helpers of the type tests.
 */
final class TypeTestSupport {

    static final String PACKAGE = "com.example.mod";
    static final String CLASS_NAME = "MyExt";

    private TypeTestSupport() {
    }

    static ExtensionType type(String id) {
        return ExtensionTypes.byId(id).orElseThrow(() -> new AssertionError("unknown type " + id));
    }

    /** All questions answered with their defaults, overridden by the given key/value pairs. */
    static ExtensionSpec spec(ExtensionType type, String... overrides) {
        Map<String, String> options = new HashMap<>();
        type.questions().forEach(q -> options.put(q.key(), q.defaultValue()));
        for (int i = 0; i < overrides.length; i += 2) {
            options.put(overrides[i], overrides[i + 1]);
        }
        return new ExtensionSpec(PACKAGE, CLASS_NAME, options);
    }

    static String render(String id, String... overrides) {
        var type = type(id);
        return type.render(spec(type, overrides));
    }

    static void assertCompiles(String source) {
        CompileHelper.compile(PACKAGE, CLASS_NAME, source);
    }

    /** Every free text answer (except identifiers) gets quote and backslash. */
    static ExtensionSpec hostileSpec(ExtensionType type) {
        var spec = spec(type);
        var options = new HashMap<>(spec.options());
        type.questions().stream()
                .filter(q -> q.choices().isEmpty() && !q.key().equals("param"))
                .forEach(q -> options.put(q.key(), java.util.stream.Stream
                        .of("a\"b\\c", "/a\"b\\c", "/a\"b", "a\"b")
                        .filter(q.validator())
                        .findFirst().orElseThrow(() -> new AssertionError("no hostile value for " + q.key()))));
        return new ExtensionSpec(PACKAGE, CLASS_NAME, options);
    }
}
