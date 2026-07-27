package com.condation.cms.core.client;

/*-
 * #%L
 * CMS Core
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

import com.condation.cms.api.client.ClientContext;
import com.condation.cms.api.client.ClientType;
import com.condation.cms.api.client.DeviceClass;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

/**
 *
 * @author thmar
 */
public class ClientContextService {

    private final UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(10000)
            .build();

    public ClientContext create(String userAgent) {
        return create(userAgent, null);
    }

    public ClientContext create(String userAgent, String acceptLanguage) {
        var parsedUserAgent = parse(userAgent);
        if (parsedUserAgent == null) {
            return new ClientContext(
                    getLocale(acceptLanguage, null),
                    DeviceClass.UNKNOWN,
                    ClientType.UNKNOWN,
                    Map.of()
            );
        }

        return new ClientContext(
                getLocale(acceptLanguage, parsedUserAgent),
                getDeviceClass(parsedUserAgent),
                getClientType(parsedUserAgent),
                getAttributes(parsedUserAgent)
        );
    }

    private UserAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgentAnalyzer.parse(userAgent);
    }

    private Locale getLocale(String acceptLanguage, UserAgent userAgent) {
        return getLocaleFromAcceptLanguage(acceptLanguage)
                .orElseGet(() -> getLocaleFromUserAgent(userAgent));
    }

    private Optional<Locale> getLocaleFromAcceptLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return Optional.empty();
        }

        try {
            return Locale.LanguageRange.parse(acceptLanguage).stream()
                    .filter(range -> range.getWeight() > 0)
                    .map(Locale.LanguageRange::getRange)
                    .filter(languageTag -> !"*".equals(languageTag))
                    .map(Locale::forLanguageTag)
                    .filter(locale -> !locale.getLanguage().isBlank())
                    .findFirst();
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Locale getLocaleFromUserAgent(UserAgent userAgent) {
        if (userAgent == null) {
            return Locale.ROOT;
        }

        var languageCode = userAgent.getValue(UserAgent.AGENT_LANGUAGE_CODE);
        if (isUnknown(languageCode)) {
            return Locale.ROOT;
        }

        var locale = Locale.forLanguageTag(languageCode.replace('_', '-'));
        return locale.getLanguage().isBlank() ? Locale.ROOT : locale;
    }

    private DeviceClass getDeviceClass(UserAgent userAgent) {
        return switch (normalize(userAgent.getValue(UserAgent.DEVICE_CLASS))) {
            case "mobile", "phone", "watch", "handheldgameconsole" -> DeviceClass.MOBILE;
            case "tablet", "ereader" -> DeviceClass.TABLET;
            case "desktop" -> DeviceClass.DESKTOP;
            default -> DeviceClass.UNKNOWN;
        };
    }

    private ClientType getClientType(UserAgent userAgent) {
        var agentClass = normalize(userAgent.getValue(UserAgent.AGENT_CLASS));
        var agentName = normalize(userAgent.getValue(UserAgent.AGENT_NAME));
        var deviceClass = normalize(userAgent.getValue(UserAgent.DEVICE_CLASS));

        if (agentClass.contains("browser") || agentClass.contains("webview")) {
            return ClientType.BROWSER;
        }
        if (isApiClient(agentClass, agentName)) {
            return ClientType.API_CLIENT;
        }
        if (agentClass.contains("robot")
                || agentClass.contains("bot")
                || agentClass.contains("crawler")
                || agentClass.contains("spider")
                || deviceClass.contains("robot")) {
            return ClientType.BOT;
        }
        return ClientType.UNKNOWN;
    }

    private boolean isApiClient(String agentClass, String agentName) {
        return agentClass.contains("app")
                || agentClass.contains("library")
                || agentClass.contains("download")
                || agentClass.contains("commandline")
                || agentClass.contains("httpclient")
                || agentClass.equals("special")
                || agentName.contains("curl")
                || agentName.contains("wget")
                || agentName.contains("postman")
                || agentName.contains("insomnia")
                || agentName.contains("httpie")
                || agentName.contains("httpclient")
                || agentName.contains("pythonrequests")
                || agentName.contains("okhttp")
                || agentName.contains("libwwwperl")
                || agentName.contains("gohttpclient")
                || agentName.contains("nodefetch")
                || agentName.contains("powershell")
                || agentName.contains("grpc");
    }

    private Map<String, Object> getAttributes(UserAgent userAgent) {
        Map<String, Object> attributes = new HashMap<>();
        userAgent.toMap().forEach(attributes::put);
        return attributes;
    }

    private boolean isUnknown(String value) {
        return value == null
                || value.isBlank()
                || UserAgent.UNKNOWN_VALUE.equalsIgnoreCase(value);
    }

    private String normalize(String value) {
        if (isUnknown(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

}
