package com.condation.cms.e2e;

/*-
 * #%L
 * integration-tests
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
import com.condation.cms.test.e2e.CMSServerExtension;
import com.condation.cms.test.e2e.HttpUtil;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author thmar
 */
@UsePlaywright
@ExtendWith(CMSServerExtension.class)
public class ExampleModuleTest {
	
	
    @Test
    void test_api() throws Exception {
        Assertions.assertThat(HttpUtil.fetchText("http://localhost:2020/api/test-api")).isEqualTo("CondationCMS test api");
    }
	
	@Test
	void test_hook (Page page) {
		page.navigate("http://localhost:2020");
        Assertions.assertThat(page.locator("body").innerHTML()).contains("<!-- example from application scope -->");
	}
	
	@Test
	void test_http_module () throws Exception {
		Assertions.assertThat(HttpUtil.fetchText("http://localhost:2020/module/example-module/world")).isEqualTo("Hello world!");
	}
	
	@Test
	void test_shortcode (Page page) throws Exception {
		page.navigate("http://localhost:2020");
        Assertions.assertThat(page.locator("body").innerHTML()).contains("<b>example from module</b>");
	}

    @Test
    void test_sse_module_endpoint() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:2020/events/example")).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (var body = response.body();
                var reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            Assertions.assertThat(response.statusCode()).isEqualTo(200);
            Assertions.assertThat(reader.readLine()).isEqualTo("event: connected");
            Assertions.assertThat(reader.readLine()).isEqualTo("data: Example module SSE connection established");
        }
    }

    @Test
    void test_sse_shared_endpoint() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:2020/events")).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (var body = response.body();
                var reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            Assertions.assertThat(response.statusCode()).isEqualTo(200);
            Assertions.assertThat(reader.readLine()).isEqualTo("event: example");
            Assertions.assertThat(reader.readLine()).isEqualTo("data: Example module event delivered via Messaging");
        }
    }

    @Test
    void test_websocket_module_endpoint() throws Exception {
        Assertions.assertThat(webSocketEcho("ws://localhost:2020/ws/echo", "hello"))
                .isEqualTo("echo: hello");
    }

    @Test
    void test_websocket_shared_endpoint() throws Exception {
        Assertions.assertThat(webSocketEcho("ws://localhost:2020/ws", "hello"))
                .isEqualTo("shared echo: hello");
    }

    private String webSocketEcho(String url, String message) throws Exception {
        var received = new CompletableFuture<String>();
        var socket = HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.request(1);
                        webSocket.sendText(message, true);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        received.complete(data.toString());
                        webSocket.request(1);
                        return CompletableFuture.completedFuture(null);
                    }
                }).get(5, TimeUnit.SECONDS);

        String response = received.get(5, TimeUnit.SECONDS);
        socket.sendClose(WebSocket.NORMAL_CLOSURE, "done").get(5, TimeUnit.SECONDS);
        return response;
    }
}
