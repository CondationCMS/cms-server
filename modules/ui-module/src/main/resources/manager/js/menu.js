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
window.addEventListener("DOMContentLoaded", () => {
	const actionElements = document.querySelectorAll('[data-action-definition]');
	actionElements.forEach(element => {
		try {
			element.addEventListener("click", (action) => {
				const definition = element.getAttribute('data-action-definition');
				try {
					const action = JSON.parse(definition);
					console.log(action)
					if (action.type === "hook") {
						executeHookAction(action)
					} else if (action.type === "script") {
						executeScriptAction(action)
					}

				} catch (e) {
					console.error('error parsing error definition', e);
				}
			})
		} catch (e) {
			console.error('', e);
		}
	});
})

const executeScriptAction = async (action) => {
  if (action.module && action.function === "runAction") {
    import(action.module)
      .then(mod => {
		if (typeof mod[action.function] === "function") {
          mod[action.function](action.parameters || {});
        } else {
          console.error("Function runAction not found", action.module);
        }
      })
      .catch(err => {
        console.error("Error loading module:", action.module, err);
      });
  }
}

const executeHookAction = async (action) => {
	var data = {
		type : action.hook
	}
	if (action.parameters) {
		data.parameters = action.parameters
	}
	const response = await fetch("/manager/hooks", {
		method: "POST",
		body: JSON.stringify(data)
	});
}
