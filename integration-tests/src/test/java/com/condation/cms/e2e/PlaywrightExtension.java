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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author thmar
 */
public class PlaywrightExtension implements BeforeAllCallback, AutoCloseable {

    private static Playwright playwright;
    private static Browser browser;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                   .get("playwright") == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
            context.getRoot()
                   .getStore(ExtensionContext.Namespace.GLOBAL)
                   .put("playwright", this);
        }
    }

    public static Browser getBrowser() { return browser; }

    @Override
    public void close() throws Exception {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
