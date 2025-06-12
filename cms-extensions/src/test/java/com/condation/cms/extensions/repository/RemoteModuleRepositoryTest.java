package com.condation.cms.extensions.repository;

/*-
 * #%L
 * cms-extensions
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.sun.net.httpserver.HttpServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoteModuleRepositoryTest {

    private HttpServer server1;
    private HttpServer server2;

    private String baseUrl1;
    private String baseUrl2;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
    public static class DummyExtensionInfo {
		String name;
		String version; 
	}

    @BeforeAll
    void setupServers() throws IOException {
        server1 = HttpServer.create(new InetSocketAddress(0), 0);
        server2 = HttpServer.create(new InetSocketAddress(0), 0);

        int port1 = server1.getAddress().getPort();
        int port2 = server2.getAddress().getPort();

        baseUrl1 = "http://localhost:" + port1 + "/main";
        baseUrl2 = "http://localhost:" + port2 + "/main";

        // Server 1: only provides moduleA
        server1.createContext("/main/moduleA/moduleA.yaml", exchange -> {
            String yaml = "name: ModuleA\nversion: 1.0";
            exchange.sendResponseHeaders(200, yaml.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(yaml.getBytes(StandardCharsets.UTF_8));
            }
        });

        // Server 2: only provides moduleB
        server2.createContext("/main/moduleB/moduleB.yaml", exchange -> {
            String yaml = "name: ModuleB\nversion: 2.0";
            exchange.sendResponseHeaders(200, yaml.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(yaml.getBytes(StandardCharsets.UTF_8));
            }
        });

        server1.start();
        server2.start();
    }

    @AfterAll
    void tearDown() {
        server1.stop(0);
        server2.stop(0);
    }

    @Test
    void shouldLoadModuleFromFirstRepository() {
        var repo = new RemoteModuleRepository<>(DummyExtensionInfo.class, List.of(baseUrl1, baseUrl2));

        Optional<DummyExtensionInfo> result = repo.getInfo("moduleA");

        Assertions.assertThat(result)
                .isPresent()
                .get()
                .satisfies(info -> {
                    Assertions.assertThat(info.getName()).isEqualTo("ModuleA");
                    Assertions.assertThat(info.getVersion()).isEqualTo("1.0");
                });
    }

    @Test
    void shouldLoadModuleFromSecondRepositoryIfNotInFirst() {
        var repo = new RemoteModuleRepository<>(DummyExtensionInfo.class, List.of(baseUrl1, baseUrl2));

        Optional<DummyExtensionInfo> result = repo.getInfo("moduleB");

        Assertions.assertThat(result)
                .isPresent()
                .get()
                .satisfies(info -> {
                    Assertions.assertThat(info.getName()).isEqualTo("ModuleB");
                    Assertions.assertThat(info.getVersion()).isEqualTo("2.0");
                });
    }

    @Test
    void shouldReturnEmptyIfModuleNotFoundInAnyRepository() {
        var repo = new RemoteModuleRepository<>(DummyExtensionInfo.class, List.of(baseUrl1, baseUrl2));

        Optional<DummyExtensionInfo> result = repo.getInfo("unknownModule");

        Assertions.assertThat(result).isNotPresent();
    }
}
