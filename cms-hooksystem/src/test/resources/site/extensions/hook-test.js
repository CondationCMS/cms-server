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

import { AuthFeature, $features } from 'system/features.mjs';

// --- action: no arguments, uses feature context ---
$hooks.registerAction(
	"test",
	(args) => {
		if ($features.has(AuthFeature)) {
			return `Hallo ${$features.get(AuthFeature).username()}`
		}
		return 'Guten Tag'
	}
)

// --- action: single named argument ---
$hooks.registerAction(
	"print_name",
	({name}) => `Hallo ${name}`
)

// --- action: single named argument ---
$hooks.registerAction(
	"print_name_args",
	(args) => `Hallo ${args.name}`
)

// --- action: multiple named arguments ---
$hooks.registerAction(
	"greet",
	({firstName, lastName}) => `${firstName} ${lastName}`
)

// --- action: multiple handlers on same hook (both results collected) ---
$hooks.registerAction("multi/action", (args) => "result1")
$hooks.registerAction("multi/action", (args) => "result2")

// --- action: priority ordering (lower number = earlier execution) ---
$hooks.registerAction("priority/action", (args) => "high", 200)
$hooks.registerAction("priority/action", (args) => "low",  100)

// --- action: no return value → must not appear in results ---
$hooks.registerAction("action/void", (args) => { /* intentionally no return */ })

// --- filter: transform string to uppercase ---
$hooks.registerFilter("filter/upper", (s) => s.toUpperCase())

// --- filter: two chained transforms, priority controls order ---
$hooks.registerFilter("filter/chain", (s) => s + "-A", 100)
$hooks.registerFilter("filter/chain", (s) => s + "-B", 200)

// --- filter: trim whitespace ---
$hooks.registerFilter("filter/trim", (s) => s.trim())
