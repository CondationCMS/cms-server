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
// state-manager.js
import { EventBus } from "./event-bus.js";
const TAB_ID_KEY = "cms-tab-id";
const AUTH_KEY = "cms-auth-token";
export const UIStateManager = {
    // Save generic tab-specific state
    setTabState(key, value) {
        sessionStorage.setItem(key, JSON.stringify(value));
    },
    getTabState(key, defaultValue = null) {
        const raw = sessionStorage.getItem(key);
        return raw ? JSON.parse(raw) : defaultValue;
    },
    setLocale(locale) {
        this.setTabState("cms-locale", locale);
        EventBus.emit("ui:localeChanged", {
            locale: locale
        });
    },
    getLocale() {
        return this.getTabState("cms-locale");
    },
    removeTabState(key) {
        sessionStorage.removeItem(key);
    },
    // Save shared auth state
    setAuthToken(token) {
        localStorage.setItem(AUTH_KEY, token);
    },
    getAuthToken() {
        return localStorage.getItem(AUTH_KEY);
    },
    clearAuthToken() {
        localStorage.removeItem(AUTH_KEY);
    },
};
