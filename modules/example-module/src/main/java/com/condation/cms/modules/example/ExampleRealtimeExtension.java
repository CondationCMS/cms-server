package com.condation.cms.modules.example;

/*-
 * #%L
 * CMS Example Module
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

import com.condation.cms.api.extensions.RealtimeExtensionPoint;
import com.condation.cms.api.feature.features.MessagingFeature;
import com.condation.cms.api.realtime.RealtimeEndpoint;
import com.condation.cms.api.realtime.RealtimeTransport;
import com.condation.cms.api.realtime.SseConnection;
import com.condation.cms.api.realtime.WebSocketConnection;
import com.condation.modules.api.annotation.Extension;

@Extension(RealtimeExtensionPoint.class)
public class ExampleRealtimeExtension extends RealtimeExtensionPoint {

    @RealtimeEndpoint(transport = RealtimeTransport.SSE)
    public void sharedEvents(SseConnection connection) {
        var topic = getContext().get(MessagingFeature.class).messaging().topic("example-module.realtime");
        var subscription = topic.subscribeWithHandle(
                message -> connection.send("example", message), String.class);
        connection.onClose(subscription::close);
        topic.publish("Example module event delivered via Messaging", com.condation.cms.api.messaging.Topic.Mode.SYNC);
    }

    @RealtimeEndpoint(transport = RealtimeTransport.SSE, path = "example")
    public void sse(SseConnection connection) {
        connection.send("connected", "Example module SSE connection established");
    }

    @RealtimeEndpoint(transport = RealtimeTransport.WEBSOCKET)
    public void sharedWebSocket(WebSocketConnection connection) {
        connection.onText(message -> connection.sendText("shared echo: " + message));
    }

    @RealtimeEndpoint(transport = RealtimeTransport.WEBSOCKET, path = "echo")
    public void websocket(WebSocketConnection connection) {
        connection.onText(message -> connection.sendText("echo: " + message));
    }
}
