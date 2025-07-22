"use strict";
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
var _a, _b, _c, _d;
const ui = {
    state: "login"
};
const setWarningMessage = (message) => {
    const alert = document.querySelector("#loginMessage");
    if (alert) {
        alert.innerHTML = message;
        alert.classList.add("alert-warning");
    }
};
const clearMessage = () => {
    const alert = document.querySelector("#loginMessage");
    if (alert) {
        alert.innerHTML = "";
        alert.classList.remove("alert-warning");
    }
};
const signIn = (e) => {
    var _a, _b;
    e.preventDefault();
    clearMessage();
    fetch(window.ui.login_url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            command: "login",
            data: {
                username: (_a = document.querySelector("#inputUsername")) === null || _a === void 0 ? void 0 : _a.value,
                password: (_b = document.querySelector("#inputPassword")) === null || _b === void 0 ? void 0 : _b.value
            }
        })
    })
        .then(response => response.json())
        .then(result => {
        var _a, _b;
        if (result.status === "2fa_required") {
            (_a = document.querySelector("#signIn")) === null || _a === void 0 ? void 0 : _a.classList.toggle("hidden");
            (_b = document.querySelector("#validate")) === null || _b === void 0 ? void 0 : _b.classList.toggle("hidden");
            ui.state = "validate";
        }
        else if (result.status === "ok") {
            window.location.href = result.redirect || window.ui.manager_url;
        }
        else {
            setWarningMessage("Login failed, maybe your credentials are incorrect. Please try again or contact your admin.");
        }
    });
};
const validate = (e) => {
    var _a;
    e.preventDefault();
    clearMessage();
    // 2FA-Feld anzeigen oder ist schon sichtbar
    const code = (_a = document.querySelector("#inputCode")) === null || _a === void 0 ? void 0 : _a.value;
    fetch(window.ui.login_url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            command: "validate",
            data: {
                code: code
            }
        })
    })
        .then(response => response.json())
        .then(validateResult => {
        if (validateResult.status === "ok") {
            window.location.href = validateResult.redirect || window.ui.manager_url;
        }
        else {
            setWarningMessage("Validation of the login code failed. Please try again or contact your admin.");
        }
    });
};
const formSubmit = (e) => {
    e.preventDefault();
    if (ui.state === "login") {
        signIn(e);
    }
    else {
        validate(e);
    }
};
if (document.querySelector("#resetButton")) {
    (_a = document.querySelector("#resetButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", (e) => {
        var _a, _b;
        ui.state = "login";
        document.querySelector("#loginForm").reset();
        (_a = document.querySelector("#signIn")) === null || _a === void 0 ? void 0 : _a.classList.remove("hidden");
        (_b = document.querySelector("#validate")) === null || _b === void 0 ? void 0 : _b.classList.add("hidden");
    });
}
if (document.querySelector("#validateButton")) {
    (_b = document.querySelector("#validateButton")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", validate);
}
(_c = document.querySelector("#signInButton")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", signIn);
(_d = document.querySelector("#loginForm")) === null || _d === void 0 ? void 0 : _d.addEventListener("submit", formSubmit);
