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

import com.condation.cms.api.extensions.RealtimeExtensionPoint;
import com.condation.cms.api.realtime.RealtimeEndpoint;
import com.condation.cms.api.realtime.RealtimeTransport;
import com.condation.cms.api.realtime.SseConnection;
import com.condation.cms.api.realtime.WebSocketConnection;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Discovers annotated handlers directly on the loaded module extension point instances. */
public class RealtimeEndpointScanner {

    public List<RealtimeEndpointRegistration> scan(
            Collection<? extends RealtimeExtensionPoint> extensionPoints) {
        var registrations = new ArrayList<RealtimeEndpointRegistration>();
        Set<String> endpointKeys = new HashSet<>();

        extensionPoints.forEach(extensionPoint -> {
            Method[] methods = extensionPoint.getClass().getMethods();
            java.util.Arrays.sort(methods, Comparator.comparing(Method::toGenericString));
            for (Method method : methods) {
                var annotation = method.getAnnotation(RealtimeEndpoint.class);
                if (annotation == null) {
                    continue;
                }
                validate(method, annotation.transport());
                String path = endpointPath(annotation.transport(), annotation.path());
                String key = annotation.transport() + ":" + path;
                boolean sharedRoot = path.equals(annotation.transport() == RealtimeTransport.SSE
                        ? "/events" : "/ws");
                if (!endpointKeys.add(key) && !sharedRoot) {
                    throw new IllegalStateException("Duplicate realtime endpoint: " + key);
                }
                registrations.add(new RealtimeEndpointRegistration(
                        annotation.transport(), path, extensionPoint, method));
            }
        });

        return List.copyOf(registrations);
    }

    private void validate(Method method, RealtimeTransport transport) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Realtime endpoint method must be public: " + method);
        }
        Class<?> expectedConnection = transport == RealtimeTransport.SSE
                ? SseConnection.class : WebSocketConnection.class;
        if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != expectedConnection) {
            throw new IllegalArgumentException("Realtime endpoint method must accept exactly one "
                    + expectedConnection.getSimpleName() + " parameter: " + method);
        }
        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException("Realtime endpoint method must return void: " + method);
        }
    }

    private String endpointPath(RealtimeTransport transport, String relativePath) {
        String normalized = relativePath == null ? "" : relativePath.trim();
        if (normalized.startsWith("/") || normalized.endsWith("/")
                || normalized.contains("//") || normalized.contains("..")
                || normalized.contains("\\") || normalized.contains("?")) {
            throw new IllegalArgumentException("Realtime endpoint path must be relative and normalized: " + relativePath);
        }
        String prefix = transport == RealtimeTransport.SSE ? "/events" : "/ws";
        return normalized.isEmpty() ? prefix : prefix + "/" + normalized;
    }
}
