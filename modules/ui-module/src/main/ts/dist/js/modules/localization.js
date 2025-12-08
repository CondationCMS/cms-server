/*-
 * #%L
 * ui-module
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
import { getLocale, setLocale } from '@cms/modules/locale-utils.js';
import { loadLocalizationsWithDefaults } from '@cms/modules/localization-loader.js';
const DEFAULT_LOCALE = 'en';
const i18n = {
    _locale: getLocale(),
    _cache: null,
    /**
     * Loads and merges remote localizations with defaults.
     */
    async init() {
        try {
            if (this._cache != null) {
                return;
            }
            this._cache = await loadLocalizationsWithDefaults();
        }
        catch (err) {
            console.warn("[i18n] Failed to load remote translations, using defaults.", err);
            this._cache = await loadLocalizationsWithDefaults();
        }
    },
    /**
     * Get current locale.
     */
    getLocale() {
        return this._locale || DEFAULT_LOCALE;
    },
    /**
     * Change the active locale and update cookie.
     * @param {string} locale
     */
    setLocale(locale) {
        setLocale(locale);
        this._locale = locale;
    },
    /**
     * Returns the translation synchronously after init.
     * @param {string} key
     * @param {string} [defaultValue]
     * @returns {string}
     */
    t(key, defaultValue) {
        const loc = this.getLocale();
        return this._cache?.[loc]?.[key]
            || this._cache?.[DEFAULT_LOCALE]?.[key]
            || defaultValue
            || key;
    },
    /**
     * Returns the translation asynchronously (no need to call init beforehand).
     * @param {string} key
     * @param {string} [defaultValue]
     * @returns {Promise<string>}
     */
    async tAsync(key, defaultValue) {
        if (!this._cache) {
            await this.init();
        }
        return this.t(key, defaultValue);
    }
};
const localizeUi = async () => {
    i18n.init();
    document.querySelectorAll("[data-cms-i18n-key]").forEach($elem => {
        const key = $elem.getAttribute("data-cms-i18n-key");
        const translation = i18n.t(key, $elem.textContent);
        if (translation) {
            $elem.textContent = translation;
        }
        else {
            // Optional: Fallback zur Default-Sprache oder Anzeige eines Platzhalters
            const fallback = localizations?.[DEFAULT_LOCALE]?.[key];
            if (fallback) {
                $elem.textContent = fallback;
            }
        }
    });
};
export { localizeUi, i18n };
