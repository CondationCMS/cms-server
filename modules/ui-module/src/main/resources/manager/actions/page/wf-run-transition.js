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
import { i18n } from "@cms/modules/localization";
import { reloadPreview } from "@cms/modules/preview.utils";
import { wfTransit } from "@cms/modules/rpc/rpc-workflow";
import { showToast } from "@cms/modules/toast";
export async function runAction(params) {
    var request = {
        uri: params.uri,
        transitionId: params.transitionId
    };
    try {
        await wfTransit(request);
        showToast({
            title: i18n.t('manager.actions.page.workflow.success.toast.title', "Workflow transition completed"),
            message: i18n.t('manager.actions.page.workflow.success.toast.message', "The workflow transition has been completed successfully."),
            type: 'success', // optional: info | success | warning | error
            timeout: 3000
        });
        reloadPreview();
    }
    catch (e) {
        showToast({
            title: i18n.t('manager.actions.page.workflow.error.toast.title', "Workflow transition failed"),
            message: i18n.t('manager.actions.page.workflow.error.toast.message', "An error occurred while transitioning the workflow."),
            type: 'error', // optional: info | success | warning | error
            timeout: 3000
        });
    }
}
