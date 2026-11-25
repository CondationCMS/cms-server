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
 * API handler mapped to a path and an HTTP method.
 */
public final class ApiHandlerType extends BaseType {

    private static final String METHOD = "method";

    public ApiHandlerType() {
        super("api-handler", "API handler (path + HTTP method)",
                Question.absolutePath("path", "API path", "/my-api"),
                Question.choice(METHOD, "HTTP method", "GET", "GET", "POST", "PUT", "DELETE"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.extensions.http.APIHandlerExtensionPoint;
                import com.condation.cms.api.extensions.http.PathMapping;
                import com.condation.modules.api.annotation.Extension;
                import java.nio.ByteBuffer;
                import java.nio.charset.StandardCharsets;
                import org.eclipse.jetty.http.pathmap.PathSpec;

                @Extension(APIHandlerExtensionPoint.class)
                public class {{class}} extends APIHandlerExtensionPoint {

                    @Override
                    public PathMapping getMapping() {
                        PathMapping mapping = new PathMapping();
                        mapping.add(PathSpec.from("{{path}}"), "{{method}}", (request, response, callback) -> {
                            response.write(true, ByteBuffer.wrap("Hello from {{class}}".getBytes(StandardCharsets.UTF_8)), callback);
                            return true;
                        });
                        return mapping;
                    }
                }
                """,
                "path", absolutePath(spec, "path"),
                METHOD, choice(spec, METHOD, "GET", "POST", "PUT", "DELETE"));
    }
}
