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
import com.condation.cms.maven.add.Question;

/**
 * Hook registration: an action or a filter.
 */
public final class HookType extends BaseType {

    public HookType() {
        super("hook", "Hook (action or filter)",
                Question.choice("kind", "Kind of hook", "action", "action", "filter"),
                Question.text("hook", "Hook name", "my/hook"),
                Question.choice("scope", "Scope", "REQUEST", "REQUEST", "APPLICATION"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        var filter = "filter".equals(spec.option("kind"));
        var scope = spec.option("scope");
        if (!scope.equals("REQUEST") && !scope.equals("APPLICATION")) {
            throw new IllegalArgumentException("Invalid scope: " + scope);
        }
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.annotations.{{annotation}};
                import com.condation.cms.api.annotations.Scope;
                import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
                import com.condation.modules.api.annotation.Extension;

                @Extension(HookSystemRegisterExtensionPoint.class)
                public class {{class}} extends HookSystemRegisterExtensionPoint {

                    @{{annotation}}(value = "{{hook}}", scope = Scope.{{scope}})
                    {{method}}
                }
                """,
                "annotation", filter ? "Filter" : "Action",
                "hook", literal(spec, "hook"),
                "scope", scope,
                "method", filter
                        ? """
                        public Object filter(Object value) {
                                // change the value and return it
                                return value;
                            }"""
                        : """
                        public String action() {
                                // the result is collected by the caller of the hook
                                return "";
                            }""");
    }
}
