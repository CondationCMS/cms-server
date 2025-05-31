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
const COOKIE_NAME = 'cms-locale';
const DEFAULT_LOCALE = 'en';
const COOKIE_MAX_AGE_DAYS = 365;

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

export function setLocale(locale) {
	const date = new Date();
	date.setTime(date.getTime() + (COOKIE_MAX_AGE_DAYS * 24 * 60 * 60 * 1000));
	document.cookie = `${COOKIE_NAME}=${encodeURIComponent(locale)};path=/;expires=${date.toUTCString()}`;
}
