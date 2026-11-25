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
 * Template function with one parameter.
 */
public final class TemplateFunctionType extends BaseType {

    private static final String PARAM = "param";

    public TemplateFunctionType() {
        super("template-function", "Template function",
                Question.text("name", "Function name", "hello"),
                Question.identifier(PARAM, "Parameter name", "name"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        var param = identifier(spec, PARAM);
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.annotations.Param;
                import com.condation.cms.api.annotations.TemplateFunction;
                import com.condation.cms.api.extensions.RegisterTemplateFunctionExtensionPoint;
                import com.condation.modules.api.annotation.Extension;

                @Extension(RegisterTemplateFunctionExtensionPoint.class)
                public class {{class}} extends RegisterTemplateFunctionExtensionPoint {

                    @TemplateFunction("{{name}}")
                    public Object templateFunction(@Param("{{param}}") Object {{param}}) {
                        return "Hello " + {{param}};
                    }
                }
                """,
                "name", literal(spec, "name"),
                PARAM, param);
    }
}
