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

const ui = {
	state: "login"
}

const setWarningMessage = (message) => {
	const alert = document.querySelector("#loginMessage");
	alert.innerHTML = message;
	alert.classList.add("alert-warning");
}
const clearMessage = () => {
	const alert = document.querySelector("#loginMessage");
	alert.innerHTML = "";
	alert.classList.remove("alert-warning");
}

const signIn = (e) => {
	e.preventDefault()
	clearMessage();
	fetch(window.ui.login_url, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			command: "login",
			data: {
				username: document.querySelector("#inputUsername").value,
				password: document.querySelector("#inputPassword").value
			}
		})
	})
			.then(response => response.json())
			.then(result => {
				if (result.status === "2fa_required") {
					document.querySelector("#signIn").classList.toggle("hidden");
					document.querySelector("#validate").classList.toggle("hidden");
					ui.state = "validate"
				} else if (result.status === "ok") {
					window.location.href = result.redirect || window.ui.manager_url;
				} else {
					setWarningMessage("Login failed, maybe your credentials are incorrect. Please try again or contact your admin.");
				}
			});
}

const validate = (e) => {
	e.preventDefault();
	clearMessage();
	// 2FA-Feld anzeigen oder ist schon sichtbar
	const code = document.querySelector("#inputCode").value;

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
				} else {
					setWarningMessage("Validation of the login code failed. Please try again or contact your admin.");
				}
			});
}

const formSubmit = (e) => {
	e.preventDefault();
	if (ui.state === "login") {
		signIn(e)
	} else {
		validate(e)
	}
}

document.querySelector("#resetButton").addEventListener("click", (e) => {
	ui.state = "login";
	document.querySelector("#loginForm").reset()
	document.querySelector("#signIn").classList.remove("hidden");
	document.querySelector("#validate").classList.add("hidden");
})

document.querySelector("#validateButton").addEventListener("click", validate)

document.querySelector("#signInButton").addEventListener("click", signIn)
document.querySelector("#loginForm").addEventListener("submit", formSubmit)
