package com.condation.cms.test.e2e;

/*-
 * #%L
 * CMS Test Server
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUtil {

    // HttpClient als Klassenattribut – thread-safe und wiederverwendbar
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))   // Verbindungs-Timeout: 10 Sekunden
            .followRedirects(HttpClient.Redirect.NORMAL) // Weiterleitungen automatisch folgen
            .build();

    /**
     * Lädt den Text-Response einer URL und gibt ihn als String zurück.
     *
     * @param url Die Ziel-URL als String
     * @return Den Response-Body als String
     * @throws Exception Bei Verbindungsfehlern oder ungültiger URL
     */
    public static String fetchText(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))                          // URL setzen
                .timeout(Duration.ofSeconds(30))               // Request-Timeout: 30 Sekunden
                .header("Accept", "text/plain, text/html, */*") // Akzeptierte Content-Types
                .GET()                                         // HTTP GET-Methode
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()           // Response-Body als String lesen
        );

        // HTTP-Fehler (4xx / 5xx) als Exception werfen
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                "HTTP-Fehler: Status " + response.statusCode() + " für URL: " + url
            );
        }

        return response.body();
    }

    // --- Beispielaufruf ---
    public static void main(String[] args) throws Exception {
        String inhalt = fetchText("https://example.com");
        System.out.println(inhalt);
    }
}
