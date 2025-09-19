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
import { i18n } from '../../js/modules/localization.js'
import { alertConfirm } from '../../js/modules/alerts.js';
import { deleteSection } from '../../js/modules/rpc/rpc-content.js';
import { showToast } from '../../js/modules/toast.js';
import { reloadPreview } from '../../js/modules/preview.utils.js';

export async function runAction(params) {

	var confimred = await alertConfirm({
		title: i18n.t("filebrowser.delete.confirm.title", "Are you sure?"),
		message: i18n.t("filebrowser.delete.confirm.message", "You won't be able to revert this!"),
		confirmText: i18n.t("filebrowser.delete.confirm.yes", "Yes, delete it!"),
		cancelText: i18n.t("filebrowser.delete.confirm.no", "No, cancel!")
	});
	if (!confimred) {
		return;
	}

	const sectionUri = params.sectionUri;
	
	deleteSection({
		uri: sectionUri
	}).then((response) => {
		if (response.error) {
			showToast({
				title: i18n.t("manager.actions.section.delete.error.title", "Error deleting section"),
				message: i18n.t("manager.actions.section.delete.error.message", "Error deleting section"),
				type: 'error',
				timeout: 3000
			});
		} else {
			showToast({
				title: i18n.t("manager.actions.section.delete.success.title", "Delete section"),
				message: i18n.t("manager.actions.section.delete.success.message", "Section deleted successfully"),
				type: 'success',
				timeout: 3000
			});
			reloadPreview();
		}
	})
}

