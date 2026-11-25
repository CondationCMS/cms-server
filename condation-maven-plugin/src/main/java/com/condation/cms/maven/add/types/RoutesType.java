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
 * Annotation based route.
 */
public final class RoutesType extends BaseType {

    public RoutesType() {
        super("routes", "Routes (@Route)",
                Question.absolutePath("path", "Route path", "/my-route"),
                Question.choice("method", "HTTP method", "GET", "GET", "POST", "PUT", "DELETE"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.annotations.Route;
                import com.condation.cms.api.extensions.http.routes.RoutesExtensionPoint;
                import com.condation.modules.api.annotation.Extension;
                import java.nio.ByteBuffer;
                import java.nio.charset.StandardCharsets;
                import org.eclipse.jetty.server.Request;
                import org.eclipse.jetty.server.Response;
                import org.eclipse.jetty.util.Callback;

                @Extension(RoutesExtensionPoint.class)
                public class {{class}} extends RoutesExtensionPoint {

                    @Route(value = "{{path}}", method = "{{method}}")
                    public boolean route(Request request, Response response, Callback callback) throws Exception {
                        response.write(true, ByteBuffer.wrap("Hello from {{class}}".getBytes(StandardCharsets.UTF_8)), callback);
                        return true;
                    }
                }
                """,
                "path", absolutePath(spec, "path"),
                "method", choice(spec, "method", "GET", "POST", "PUT", "DELETE"));
    }
}
