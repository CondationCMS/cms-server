package com.condation.cms.test.e2e;

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
}