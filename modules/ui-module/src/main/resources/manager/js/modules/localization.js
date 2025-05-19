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

import { loadLocalizations } from '/manager/js/modules/rpc-i18n.js'


const COOKIE_NAME = 'cms-locale';
const DEFAULT_LOCALE = 'en';
const COOKIE_MAX_AGE_DAYS = 365;

/**
 * Set the locale cookie.
 * @param {string} locale - The locale to be saved (e.g. 'en', 'de').
 */
export function setLocale(locale) {
  const date = new Date();
  date.setTime(date.getTime() + (COOKIE_MAX_AGE_DAYS * 24 * 60 * 60 * 1000));
  document.cookie = `${COOKIE_NAME}=${encodeURIComponent(locale)};path=/;expires=${date.toUTCString()}`;
}

/**
 * Get the locale from cookie. Returns default if not set.
 * @returns {string} The stored locale or the default.
 */
export function getLocale() {
  const cookies = document.cookie.split(';');
  for (let c of cookies) {
    const [key, value] = c.trim().split('=');
    if (key === COOKIE_NAME) {
      return decodeURIComponent(value);
    }
  }
  return DEFAULT_LOCALE;
}

/**
 * Initialize locale, returns current value (cookie or default).
 * @returns {string} The initialized locale.
 */
export function initLocale() {
  const locale = getLocale();
  // Optionally: Set it if not yet stored
  if (!document.cookie.includes(`${COOKIE_NAME}=`)) {
    setLocale(locale);
  }
  return locale;
}

const localizeUi = async () => {
	let locale = getLocale();

	const localizations = (await loadLocalizations()).result;

	document.querySelectorAll("[data-cms-i18n-key]").forEach($elem => {
		const key = $elem.getAttribute("data-cms-i18n-key");
		const translation = localizations?.[locale]?.[key];

		console.log(localizations)
		console.log("translate", key, translation)

		if (translation) {
			$elem.textContent = translation;
		} else {
			// Optional: Fallback zur Default-Sprache oder Anzeige eines Platzhalters
			const fallback = localizations?.["en"]?.[key];
			if (fallback) {
				$elem.textContent = fallback;
			}
		}
	});
}

export { localizeUi };
