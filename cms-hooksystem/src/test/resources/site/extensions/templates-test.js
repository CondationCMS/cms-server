/*-
 * #%L
 * CMS Extensions
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

import { $hooks } from 'system/hooks.mjs';

// --- template functions ---
$hooks.registerAction("system/template/function", ({functions}) => {

	// no parameters
	functions.put("hello", () => "Hello World")

	// destructured parameter
	functions.put("greet", ({name = "stranger"}) => `Hello ${name}`)

	// multiple parameters
	functions.put("full_name", ({firstName, lastName}) => `${firstName} ${lastName}`)

	// explicit namespace
	functions.put("theme", "version", () => "1.0.0")

})

// --- template components ---
$hooks.registerAction("system/template/component", ({components}) => {

	// no parameters
	components.put("badge", () => "<span class='badge'>badge</span>")

	// destructured parameter
	components.put("alert", ({message = "default"}) => `<div class='alert'>${message}</div>`)

	// explicit namespace
	components.put("theme", "card", ({title}) => `<div class='card'>${title}</div>`)

})
