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

import com.condation.cms.api.request.visitor.VisitorType;
import com.condation.cms.api.request.visitor.DeviceClass;
import java.util.Locale;
import nl.basjes.parse.useragent.UserAgent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VisitorContextServiceTest {

    private static VisitorContextService service;

    @BeforeAll
    static void setUp() {
        service = new VisitorContextService();
    }

    @Test
    void createsDesktopBrowserContext() {
        var context = service.create(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Safari/537.36"
        );

        Assertions.assertThat(context.deviceClass()).isEqualTo(DeviceClass.DESKTOP);
        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.BROWSER);
        Assertions.assertThat(context.attributes())
                .containsEntry(UserAgent.AGENT_NAME, "Chrome")
                .containsEntry(UserAgent.OPERATING_SYSTEM_NAME, "Windows NT");
    }

    @Test
    void createsMobileBrowserContext() {
        var context = service.create(
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Mobile Safari/537.36"
        );

        Assertions.assertThat(context.deviceClass()).isEqualTo(DeviceClass.MOBILE);
        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.BROWSER);
    }

    @Test
    void createsBotContext() {
        var context = service.create(
                "Mozilla/5.0 (compatible; Googlebot/2.1; "
                + "+http://www.google.com/bot.html)"
        );

        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.BOT);
    }

    @Test
    void createsApiClientContext() {
        var context = service.create("curl/8.7.1");

        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.API_CLIENT);
        Assertions.assertThat(context.attributes())
                .containsEntry(UserAgent.AGENT_NAME, "Curl");
    }

    @Test
    void keepsUserAgentLanguageInAttributes() {
        var context = service.create(
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_15_7; en-US) "
                + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15"
        );

        Assertions.assertThat(context.languages()).isEmpty();
        Assertions.assertThat((String) context.attributes().get(UserAgent.AGENT_LANGUAGE_CODE))
                .isEqualToIgnoringCase("en-US");
    }

    @Test
    void keepsAcceptLanguagePreferencesOrderedByQuality() {
        var context = service.create(
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_15_7; en-US) "
                + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
                "en-US;q=0.7, de-DE;q=0.9, de;q=0.8"
        );

        Assertions.assertThat(context.languages().getFirst().getRange()).isEqualTo("de-de");
        Assertions.assertThat(context.languages().getFirst().getWeight()).isEqualTo(0.9);
        Assertions.assertThat(context.languages())
                .extracting(Locale.LanguageRange::getRange)
                .contains("de", "en-us");
    }

    @Test
    void extractsAcceptLanguageWithoutUserAgent() {
        var context = service.create(null, "fr-FR, fr;q=0.9");

        Assertions.assertThat(context.languages().getFirst().getRange()).isEqualTo("fr-fr");
        Assertions.assertThat(context.languages().getFirst().getWeight()).isEqualTo(1.0);
        Assertions.assertThat(context.deviceClass()).isEqualTo(DeviceClass.UNKNOWN);
        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.UNKNOWN);
        Assertions.assertThat(context.attributes()).isEmpty();
    }

    @Test
    void fallsBackToUserAgentForInvalidAcceptLanguage() {
        var context = service.create(
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_15_7; en-US) "
                + "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
                "not_a_valid_language;q=broken"
        );

        Assertions.assertThat(context.languages()).isEmpty();
        Assertions.assertThat((String) context.attributes().get(UserAgent.AGENT_LANGUAGE_CODE))
                .isEqualToIgnoringCase("en-US");
    }

    @Test
    void keepsWildcardAndZeroWeightForTargeting() {
        var context = service.create(null, "de-DE, *;q=0");

        Assertions.assertThat(context.languages())
                .anySatisfy(language -> {
                    Assertions.assertThat(language.getRange()).isEqualTo("*");
                    Assertions.assertThat(language.getWeight()).isZero();
                });
    }

    @Test
    void createsEmptyContextForMissingUserAgent() {
        var context = service.create(null);

        Assertions.assertThat(context.languages()).isEmpty();
        Assertions.assertThat(context.deviceClass()).isEqualTo(DeviceClass.UNKNOWN);
        Assertions.assertThat(context.visitorType()).isEqualTo(VisitorType.UNKNOWN);
        Assertions.assertThat(context.attributes()).isEmpty();
    }
}
