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

import { listFiles, createFolder, createFile, deleteFile, deleteFolder } from '/manager/js/modules/rpc-files.js'
import { createPage, deletePage } from '/manager/js/modules/rpc-page.js'
import { loadPreview, getPageTemplates } from '/manager/js/modules/ui-helpers.js'
import { i18n } from '/manager/js/modules/localization.js';
import { alertSelect, alertError, alertConfirm } from '/manager/js/modules/alerts.js'

import { showToast } from '/manager/js/modules/toast.js'

import { State } from '/manager/js/modules/state.js'

const state = new State({
	options: null,
	currentFolder: ""
});




const initDefaults = (options) => {
	if (!options) {
		options = {};
	}
	state.options = Object.assign({}, defaultOptions, options);

};
export { initDefaults };
