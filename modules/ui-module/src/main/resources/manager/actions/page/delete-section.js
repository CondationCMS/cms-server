/*-
 * #%L
 * UI Module
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
import { i18n } from '@cms/modules/localization.js';
import { alertConfirm } from '@cms/modules/alerts.js';
import { deleteSection } from '@cms/modules/rpc/rpc-content.js';
import { showToast } from '@cms/modules/toast.js';
import { reloadPreview } from '@cms/modules/preview.utils.js';
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
    try {
        await deleteSection({
            uri: sectionUri
        });
        showToast({
            title: i18n.t("manager.actions.section.delete.success.title", "Delete item"),
            message: i18n.t("manager.actions.section.delete.success.message", "Item deleted successfully"),
            type: 'success',
            timeout: 3000
        });
        reloadPreview();
    }
    catch (e) {
        showToast({
            title: i18n.t("manager.actions.section.delete.error.title", "Error deleting item"),
            message: i18n.t("manager.actions.section.delete.error.message", "Error deleting item"),
            type: 'error',
            timeout: 3000
        });
    }
}
