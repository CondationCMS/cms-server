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

import com.condation.cms.api.realtime.SseConnection;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/** Handles the SSE paths discovered from loaded module extension points. */
@Slf4j
public class JettySseEndpointHandler extends Handler.Abstract {

    private final Map<String, java.util.List<RealtimeEndpointRegistration>> endpoints;

    public JettySseEndpointHandler(Iterable<RealtimeEndpointRegistration> registrations) {
        var found = new java.util.HashMap<String, java.util.List<RealtimeEndpointRegistration>>();
        registrations.forEach(registration -> {
            if (registration.transport() == com.condation.cms.api.realtime.RealtimeTransport.SSE) {
                found.computeIfAbsent(registration.path(), _ -> new java.util.ArrayList<>()).add(registration);
            }
        });
        this.endpoints = found.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> java.util.List.copyOf(entry.getValue())));
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (!"GET".equals(request.getMethod())) {
            Response.writeError(request, response, callback, 405);
            return true;
        }
        String path = Request.getPathInContext(request);
        if (!path.startsWith("/events")) {
            path = path.isEmpty() || "/".equals(path) ? "/events" : "/events" + path;
        }
        log.debug("SSE request for path {}; registered paths: {}", path, endpoints.keySet());
        var registrations = endpoints.get(path);
        if (registrations == null) {
            Response.writeError(request, response, callback, 404);
            return true;
        }

        response.setStatus(200);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/event-stream; charset=utf-8");
        response.getHeaders().put(HttpHeader.CACHE_CONTROL, "no-cache, no-transform");
        response.getHeaders().put("X-Content-Type-Options", "nosniff");

        var connection = new JettySseConnection(response, callback);
        try {
            for (var registration : registrations) {
                registration.method().invoke(registration.extensionPoint(), connection);
            }
        } catch (InvocationTargetException exception) {
            connection.closeWithError(exception.getCause());
        } catch (ReflectiveOperationException exception) {
            connection.closeWithError(exception);
        }
        return true;
    }

    private static final class JettySseConnection implements SseConnection {
        private final Response response;
        private final Callback requestCallback;
        private final Queue<String> pending = new ArrayDeque<>();
        private boolean writing;
        private boolean closeRequested;
        private final AtomicBoolean completed = new AtomicBoolean();
        private final CopyOnWriteArrayList<Runnable> closeHandlers = new CopyOnWriteArrayList<>();

        private JettySseConnection(Response response, Callback requestCallback) {
            this.response = response;
            this.requestCallback = requestCallback;
        }

        @Override
        public synchronized void send(String event, String data) {
            if (closeRequested || completed.get()) {
                return;
            }
            StringBuilder frame = new StringBuilder();
            if (event != null && !event.isBlank()) {
                frame.append("event: ").append(clean(event)).append('\n');
            }
            if (data != null) {
                for (String line : data.replace("\r", "").split("\n", -1)) {
                    frame.append("data: ").append(line).append('\n');
                }
            }
            frame.append('\n');
            pending.add(frame.toString());
            writeNext();
        }

        @Override
        public synchronized void close() {
            if (closeRequested || completed.get()) {
                return;
            }
            closeRequested = true;
            writeNext();
        }

        @Override
        public void onClose(Runnable handler) {
            if (handler == null) {
                return;
            }
            if (completed.get()) {
                handler.run();
                return;
            }
            closeHandlers.add(handler);
            if (completed.get() && closeHandlers.remove(handler)) {
                handler.run();
            }
        }

        private synchronized void closeWithError(Throwable error) {
            if (completed.compareAndSet(false, true)) {
                notifyCloseHandlers();
                requestCallback.failed(error);
            }
        }

        private void writeNext() {
            if (writing || completed.get()) {
                return;
            }
            String frame = pending.poll();
            if (frame == null && !closeRequested) {
                return;
            }
            writing = true;
            boolean last = frame == null;
            ByteBuffer bytes = ByteBuffer.wrap(frame == null ? new byte[0]
                    : frame.getBytes(StandardCharsets.UTF_8));
            response.write(last, bytes, new Callback() {
                @Override
                public void succeeded() {
                    synchronized (JettySseConnection.this) {
                        writing = false;
                        if (last) {
                            if (completed.compareAndSet(false, true)) {
                                notifyCloseHandlers();
                                requestCallback.succeeded();
                            }
                        } else {
                            writeNext();
                        }
                    }
                }

                @Override
                public void failed(Throwable failure) {
                    if (completed.compareAndSet(false, true)) {
                        notifyCloseHandlers();
                        requestCallback.failed(failure);
                    }
                }
            });
        }

        private static String clean(String value) {
            return value.replace("\r", "").replace("\n", "");
        }

        private void notifyCloseHandlers() {
            closeHandlers.forEach(handler -> {
                try {
                    handler.run();
                } catch (Exception exception) {
                    log.warn("SSE close handler failed", exception);
                }
            });
            closeHandlers.clear();
        }
    }
}
