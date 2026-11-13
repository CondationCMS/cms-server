package com.condation.cms.server.realtime;

/*-
 * #%L
 * CMS Server
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

import com.condation.cms.api.realtime.WebSocketConnection;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.ServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.ServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.WebSocketCreator;

@Slf4j
public class JettyWebSocketCreator implements WebSocketCreator {

    private final Map<String, java.util.List<RealtimeEndpointRegistration>> endpoints;

    public JettyWebSocketCreator(Iterable<RealtimeEndpointRegistration> registrations) {
        var found = new java.util.HashMap<String, java.util.List<RealtimeEndpointRegistration>>();
        registrations.forEach(registration -> {
            if (registration.transport() == com.condation.cms.api.realtime.RealtimeTransport.WEBSOCKET) {
                found.computeIfAbsent(registration.path(), _ -> new java.util.ArrayList<>()).add(registration);
            }
        });
        this.endpoints = found.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> java.util.List.copyOf(entry.getValue())));
    }

    @Override
    public Object createWebSocket(ServerUpgradeRequest request, ServerUpgradeResponse response,
            org.eclipse.jetty.util.Callback callback) {
        String path = org.eclipse.jetty.server.Request.getPathInContext(request);
        log.debug("WebSocket upgrade for path {}; registered paths: {}", path, endpoints.keySet());
        var registrations = endpoints.get(path);
        if (registrations == null) {
            return null;
        }
        log.debug("Creating endpoint for {}", path);
        return new Endpoint(registrations);
    }

    public static final class Endpoint implements Session.Listener.AutoDemanding {
        private final java.util.List<RealtimeEndpointRegistration> registrations;
        private final java.util.List<Consumer<String>> textHandlers = new java.util.concurrent.CopyOnWriteArrayList<>();
        private final CopyOnWriteArrayList<Runnable> closeHandlers = new CopyOnWriteArrayList<>();
        private final AtomicBoolean closed = new AtomicBoolean();
        private volatile Session session;

        public Endpoint(java.util.List<RealtimeEndpointRegistration> registrations) {
            this.registrations = registrations;
        }

        @Override
        public void onWebSocketOpen(Session session) {
            log.debug("WebSocket connected to {}", session.getUpgradeRequest().getRequestURI().getPath());
            this.session = session;
            WebSocketConnection connection = new WebSocketConnection() {
                @Override
                public void sendText(String message) {
                    if (Endpoint.this.session != null && Endpoint.this.session.isOpen()) {
                        Endpoint.this.session.sendText(message, Callback.NOOP);
                    }
                }

                @Override
                public void onText(Consumer<String> handler) {
                    if (handler != null) {
                        textHandlers.add(handler);
                    }
                }

                @Override
                public void onClose(Runnable handler) {
                    addCloseHandler(handler);
                }

                @Override
                public void close() {
                    if (Endpoint.this.session != null) {
                        Endpoint.this.session.close(1000, "closed", Callback.NOOP);
                    }
                }
            };
            try {
                for (var registration : registrations) {
                    registration.method().invoke(registration.extensionPoint(), connection);
                }
            } catch (InvocationTargetException exception) {
                log.error("Realtime WebSocket endpoint failed", exception.getCause());
                session.close(1011, "endpoint failed", Callback.NOOP);
            } catch (ReflectiveOperationException exception) {
                log.error("Could not invoke realtime WebSocket endpoint", exception);
                session.close(1011, "endpoint failed", Callback.NOOP);
            }
        }

        @Override
        public void onWebSocketText(String message) {
            try {
                textHandlers.forEach(handler -> handler.accept(message));
            } catch (Exception exception) {
                log.warn("Realtime WebSocket message handler failed", exception);
                close(1011, "message handler failed");
            }
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason, Callback callback) {
            log.debug("WebSocket closed with {}: {}", statusCode, reason);
            session = null;
            notifyCloseHandlers();
            callback.succeed();
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            log.warn("WebSocket error", cause);
            notifyCloseHandlers();
        }

        private void close(int code, String reason) {
            Session current = session;
            if (current != null) {
                current.close(code, reason, Callback.NOOP);
            }
        }

        private void addCloseHandler(Runnable handler) {
            if (handler == null) {
                return;
            }
            if (closed.get()) {
                handler.run();
                return;
            }
            closeHandlers.add(handler);
            if (closed.get() && closeHandlers.remove(handler)) {
                handler.run();
            }
        }

        private void notifyCloseHandlers() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            closeHandlers.forEach(handler -> {
                try {
                    handler.run();
                } catch (Exception exception) {
                    log.warn("WebSocket close handler failed", exception);
                }
            });
            closeHandlers.clear();
        }
    }
}
