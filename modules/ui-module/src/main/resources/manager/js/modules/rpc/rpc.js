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
import { i18n } from "../localization.js";
const executeRemoteCall = async (options) => {
    return executeRemoteMethodCall(options.method, options.parameters);
};
const executeRemoteMethodCall = async (method, parameters) => {
    var data = {
        method: method,
        parameters: parameters
    };
    var response = await fetch(window.manager.baseUrl + "/rpc", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-Token': window.manager.csrfToken
        },
        body: JSON.stringify(data)
    });
    if (response.status === 403) {
        alert(i18n.t("ui.redirect.login", "You where logged out due to inactivity. Please log in again."));
        window.location.href = window.manager.baseUrl + "/login";
        return;
    }
    return await response.json();
};
export { executeRemoteCall, executeRemoteMethodCall };
