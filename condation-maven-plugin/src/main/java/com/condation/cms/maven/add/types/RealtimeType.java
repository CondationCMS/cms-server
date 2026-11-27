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
 * Realtime endpoint (server sent events or web socket).
 */
public final class RealtimeType extends BaseType {

    private static final String TRANSPORT = "transport";

    public RealtimeType() {
        super("realtime", "Realtime endpoint (SSE / WebSocket)",
                Question.choice(TRANSPORT, "Transport", "SSE", "SSE", "WEBSOCKET"),
                Question.relativePath("path", "Endpoint path", "events"));
    }

    @Override
    public String render(ExtensionSpec spec) {
        var transport = spec.option(TRANSPORT);
        var websocket = "WEBSOCKET".equals(transport);
        if (!websocket && !"SSE".equals(transport)) {
            throw new IllegalArgumentException("Invalid transport: " + transport);
        }
        return fill(spec, """
                package {{package}};

                import com.condation.cms.api.extensions.RealtimeExtensionPoint;
                import com.condation.cms.api.realtime.RealtimeEndpoint;
                import com.condation.cms.api.realtime.RealtimeTransport;
                import com.condation.cms.api.realtime.{{connection}};
                import com.condation.modules.api.annotation.Extension;

                @Extension(RealtimeExtensionPoint.class)
                public class {{class}} extends RealtimeExtensionPoint {

                    @RealtimeEndpoint(transport = RealtimeTransport.{{transport}}, path = "{{path}}")
                    public void endpoint({{connection}} connection) {
                        {{body}}
                    }
                }
                """,
                TRANSPORT, transport,
                "connection", websocket ? "WebSocketConnection" : "SseConnection",
                "path", relativePath(spec, "path"),
                "body", websocket
                        ? "connection.onText(message -> connection.sendText(\"echo: \" + message));"
                        : "connection.send(\"connected\", \"SSE connection established\");");
    }
}
