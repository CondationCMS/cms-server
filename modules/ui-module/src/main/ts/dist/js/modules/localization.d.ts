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
