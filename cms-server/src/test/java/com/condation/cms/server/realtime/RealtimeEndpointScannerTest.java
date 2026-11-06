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
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealtimeEndpointScannerTest {

    private final RealtimeEndpointScanner scanner = new RealtimeEndpointScanner();

    @Test
    void discoversMultipleAnnotatedMethodsOnOneExtensionPoint() {
        var registrations = scanner.scan(List.of(new ExampleRealtimeExtension()));

        assertThat(registrations).extracting(RealtimeEndpointRegistration::path)
                .containsExactlyInAnyOrder("/events", "/ws/echo");
        assertThat(registrations).extracting(RealtimeEndpointRegistration::transport)
                .containsExactlyInAnyOrder(RealtimeTransport.SSE, RealtimeTransport.WEBSOCKET);
    }

    @Test
    void rejectsHandlerMethodsWithUnsupportedSignatures() {
        assertThatThrownBy(() -> scanner.scan(List.of(new InvalidRealtimeExtension())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must accept exactly one");
    }

    @Test
    void rejectsDuplicatePathsForSameTransport() {
        assertThatThrownBy(() -> scanner.scan(List.of(new DuplicateRealtimeExtension())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate realtime endpoint");
    }

    @Test
    void allowsMultipleModulesToContributeToSharedRootEndpoints() {
        var registrations = scanner.scan(List.of(new SharedRootExtension(), new SharedRootExtension()));

        assertThat(registrations).hasSize(2)
                .allSatisfy(registration -> assertThat(registration.path()).isEqualTo("/events"));
    }

    public static class ExampleRealtimeExtension extends RealtimeExtensionPoint {
        @RealtimeEndpoint(transport = RealtimeTransport.SSE)
        public void events(SseConnection connection) {
        }

        @RealtimeEndpoint(transport = RealtimeTransport.WEBSOCKET, path = "echo")
        public void echo(WebSocketConnection connection) {
        }
    }

    public static class InvalidRealtimeExtension extends RealtimeExtensionPoint {
        @RealtimeEndpoint(transport = RealtimeTransport.SSE)
        public void invalid(String value) {
        }
    }

    public static class DuplicateRealtimeExtension extends RealtimeExtensionPoint {
        @RealtimeEndpoint(transport = RealtimeTransport.SSE, path = "updates")
        public void first(SseConnection connection) {
        }

        @RealtimeEndpoint(transport = RealtimeTransport.SSE, path = "updates")
        public void second(SseConnection connection) {
        }
    }

    public static class SharedRootExtension extends RealtimeExtensionPoint {
        @RealtimeEndpoint(transport = RealtimeTransport.SSE)
        public void contribute(SseConnection connection) {
        }
    }
}
