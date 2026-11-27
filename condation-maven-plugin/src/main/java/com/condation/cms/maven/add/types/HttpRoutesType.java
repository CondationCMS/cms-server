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
 * HTTP route mapped to a path.
 */
public final class HttpRoutesType extends BaseType {

    public HttpRoutesType() {
        super("http-routes", "HTTP routes (path)",
                Question.absolutePath("path", "Path", "/my-route"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.extensions.HttpRoutesExtensionPoint;
                import com.condation.cms.api.extensions.Mapping;
                import com.condation.modules.api.annotation.Extension;
                import java.nio.ByteBuffer;
                import java.nio.charset.StandardCharsets;
                import org.eclipse.jetty.http.pathmap.PathSpec;

                @Extension(HttpRoutesExtensionPoint.class)
                public class {{class}} extends HttpRoutesExtensionPoint {

                    @Override
                    public Mapping getMapping() {
                        Mapping mapping = new Mapping();
                        mapping.add(PathSpec.from("{{path}}"), (request, response, callback) -> {
                            response.write(true, ByteBuffer.wrap("Hello from {{class}}".getBytes(StandardCharsets.UTF_8)), callback);
                            return true;
                        });
                        return mapping;
                    }
                }
                """,
                "path", absolutePath(spec, "path"));
    }
}
