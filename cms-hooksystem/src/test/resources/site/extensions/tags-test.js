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

$hooks.registerAction("system/content/shortCodes", ({shortCodes}) => {

	// default namespace (ext) — no parameters
	shortCodes.put("hello", () => "Hello World")

	// default namespace (ext) — destructured parameter
	shortCodes.put("greet", ({name = "stranger"}) => `Hello ${name}`)

	// explicit namespace
	shortCodes.put("theme", "info", () => "theme-info")

	// multiple parameters
	shortCodes.put("full_name", ({firstName, lastName}) => `${firstName} ${lastName}`)

})
