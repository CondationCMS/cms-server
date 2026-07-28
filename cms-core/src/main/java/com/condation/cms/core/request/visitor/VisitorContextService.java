package com.condation.cms.core.request.visitor;

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

import com.condation.cms.api.request.visitor.VisitorContext;
import com.condation.cms.api.request.visitor.VisitorType;
import com.condation.cms.api.request.visitor.DeviceClass;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

/**
 *
 * @author thmar
 */
public class VisitorContextService {

    private final UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(10000)
            .build();

    public VisitorContext create(String userAgent) {
        return create(userAgent, null);
    }

    public VisitorContext create(String userAgent, String acceptLanguage) {
        var parsedUserAgent = parse(userAgent);
        if (parsedUserAgent == null) {
            return new VisitorContext(
                    getLanguages(acceptLanguage),
                    DeviceClass.UNKNOWN,
                    VisitorType.UNKNOWN,
                    Map.of()
            );
        }

        return new VisitorContext(
                getLanguages(acceptLanguage),
                getDeviceClass(parsedUserAgent),
                getVisitorType(parsedUserAgent),
                getAttributes(parsedUserAgent)
        );
    }

    private UserAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgentAnalyzer.parse(userAgent);
    }

    private List<Locale.LanguageRange> getLanguages(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return List.of();
        }

        try {
            return List.copyOf(Locale.LanguageRange.parse(acceptLanguage));
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
    }

    private DeviceClass getDeviceClass(UserAgent userAgent) {
        return switch (normalize(userAgent.getValue(UserAgent.DEVICE_CLASS))) {
            case "mobile", "phone", "watch", "handheldgameconsole" -> DeviceClass.MOBILE;
            case "tablet", "ereader" -> DeviceClass.TABLET;
            case "desktop" -> DeviceClass.DESKTOP;
            default -> DeviceClass.UNKNOWN;
        };
    }

    private VisitorType getVisitorType(UserAgent userAgent) {
        var agentClass = normalize(userAgent.getValue(UserAgent.AGENT_CLASS));
        var agentName = normalize(userAgent.getValue(UserAgent.AGENT_NAME));
        var deviceClass = normalize(userAgent.getValue(UserAgent.DEVICE_CLASS));

        if (agentClass.contains("browser") || agentClass.contains("webview")) {
            return VisitorType.BROWSER;
        }
        if (isApiClient(agentClass, agentName)) {
            return VisitorType.API_CLIENT;
        }
        if (agentClass.contains("robot")
                || agentClass.contains("bot")
                || agentClass.contains("crawler")
                || agentClass.contains("spider")
                || deviceClass.contains("robot")) {
            return VisitorType.BOT;
        }
        return VisitorType.UNKNOWN;
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
