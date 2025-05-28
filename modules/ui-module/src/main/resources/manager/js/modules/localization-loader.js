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
import { loadLocalizations } from '/manager/js/modules/rpc-i18n.js'

const DEFAULT_LOCALIZATIONS = {
	en: {
		"ui.filebrowser.filename": "Filename",
		"ui.filebrowser.title": "Filesystem",
		"menu.settings": "Settings",
		"menu.settings.logout": "Logout"
	},
	de: {
		"ui.filebrowser.filename": "Dateiname",
		"ui.filebrowser.title": "Dateisystem",
		"menu.settings": "Einstellungen",
		"menu.settings.logout": "Abmelden"
	}
};

const loadLocalizationsWithDefaults = async () => {
	try {
		const response = (await loadLocalizations()).result;
		// Merge server response into defaults
		for (const lang in response) {
			DEFAULT_LOCALIZATIONS[lang] = {
				...DEFAULT_LOCALIZATIONS[lang],
				...response[lang]
			};
		}
	} catch (e) {
		console.warn("Could not load remote translations, falling back to defaults.", e);
	}
	return DEFAULT_LOCALIZATIONS;
};

export {loadLocalizationsWithDefaults}
