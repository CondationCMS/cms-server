/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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
export function localizeUi(): Promise<void>;
export namespace i18n {
    let _locale: any;
    let _cache: any;
    /**
     * Loads and merges remote localizations with defaults.
     */
    function init(): Promise<void>;
    /**
     * Get current locale.
     */
    function getLocale(): any;
    /**
     * Change the active locale and update cookie.
     * @param {string} locale
     */
    function setLocale(locale: string): void;
    /**
     * Returns the translation synchronously after init.
     * @param {string} key
     * @param {string} [defaultValue]
     * @returns {string}
     */
    function t(key: string, defaultValue?: string): string;
    /**
     * Returns the translation asynchronously (no need to call init beforehand).
     * @param {string} key
     * @param {string} [defaultValue]
     * @returns {Promise<string>}
     */
    function tAsync(key: string, defaultValue?: string): Promise<string>;
}
