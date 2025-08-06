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
const executeScriptAction = async (action) => {
  if (action.module && action.function === "runAction") {
	var modulePath = patchManagerPath(action.module, window.manager.baseUrl);
    import(modulePath)
      .then(mod => {
		if (typeof mod[action.function] === "function") {
          mod[action.function](action.parameters || {});
        } else {
          console.error("Function runAction not found", action.module);
        }
      })
      .catch(err => {
        if (err && (err.status === 403 || (err.message && err.message.includes("403")))) {
          alert(i18n.t("ui.redirect.login", "You where logged out due to inactivity. Please log in again."));
          window.location.href = window.manager.baseUrl + "/login";
        } else {
          console.error("Error loading module:", action.module, err);
        }
      });
  }
}

const executeHookAction = async (action) => {
	var data = {
		hook : action.hook
	}
	if (action.parameters) {
		data.parameters = action.parameters
	}
	const response = await fetch(window.manager.baseUrl + "/hooks", {
    headers: {
			'Content-Type': 'application/json',
			'X-CSRF-Token': window.manager.csrfToken
		},
		method: "POST",
		body: JSON.stringify(data)
	});
}

/**
 * Patches a relative path so that it's correctly prefixed with the given manager base path.
 * 
 * @param {string} relativePath e.g. "/manager/module"
 * @param {string} managerBasePath e.g. "/manager" or "/de/manager"
 * @returns {string} e.g. "/de/manager/module"
 */
const patchManagerPath = (relativePath, managerBasePath) => {
    if (!relativePath || !managerBasePath) {
        throw new Error("Both paths must be provided.");
    }

    // Remove trailing slash from base path if present
    const base = managerBasePath.endsWith('/') ? managerBasePath.slice(0, -1) : managerBasePath;

    // Ensure relative path starts with a slash
    const rel = relativePath.startsWith('/') ? relativePath : '/' + relativePath;

    // If the relative path already starts with the base, avoid double prefixing
    if (rel.startsWith(base)) {
        return rel;
    }

    return base + rel;
}

/**
 * Patches a path with the context path, if not already present.
 *
 * @param {string} path - The original path (e.g. "/assets/images/test.jpg").
 * @returns {string} - The patched path with context prefix if needed.
 */
const patchPathWithContext = (path) => {
  const contextPath = window.manager.contextPath || "/";
  
  // Normalize context path (remove trailing slash if not just "/")
  const normalizedContext = contextPath !== "/" ? contextPath.replace(/\/+$/, "") : "";

  // Check if the path already starts with the context
  if (normalizedContext && path.startsWith(normalizedContext + "/")) {
    return path;
  }

  // Patch path (avoid double slashes)
  return normalizedContext + (path.startsWith("/") ? path : "/" + path);
}
