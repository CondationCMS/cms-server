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
 * Shortcode with one parameter.
 */
public final class ShortCodeType extends BaseType {

    private static final String PARAM = "param";

    public ShortCodeType() {
        super("shortcode", "Shortcode",
                Question.text("name", "Shortcode name", "hello"),
                Question.identifier(PARAM, "Parameter name", "name"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        var param = identifier(spec, PARAM);
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.annotations.Param;
                import com.condation.cms.api.annotations.ShortCode;
                import com.condation.cms.api.extensions.RegisterShortCodesExtensionPoint;
                import com.condation.modules.api.annotation.Extension;

                @Extension(RegisterShortCodesExtensionPoint.class)
                public class {{class}} extends RegisterShortCodesExtensionPoint {

                    @ShortCode("{{name}}")
                    public String shortCode(@Param("{{param}}") Object {{param}}) {
                        return "<b>Hello " + {{param}} + "</b>";
                    }
                }
                """,
                "name", literal(spec, "name"),
                PARAM, param);
    }
}
