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
import { openModal } from '@cms/modules/modal.js';
import { createForm } from '@cms/modules/form/forms.js';
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js';
import { getContentNode, getContent, setContent } from '@cms/modules/rpc/rpc-content.js';
import { i18n } from '@cms/modules/localization.js';
import { showToast } from '@cms/modules/toast.js';
// hook.js
export async function runAction(params) {
    var uri = null;
    if (params.uri) {
        uri = params.uri;
    }
    else {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        });
        uri = contentNode.result.uri;
    }
    try {
        const nodeContent = await getContent({
            uri: uri
        });
        const form = createForm({
            fields: [
                {
                    type: params.editor,
                    name: 'content',
                    title: 'Main content',
                    height: '80%'
                }
            ],
            values: {
                "content": nodeContent?.result?.content
            }
        });
        openModal({
            title: 'Edit Content',
            body: 'modal body',
            form: form,
            fullscreen: true,
            onCancel: (event) => { },
            onOk: async (event) => {
                var updateData = form.getData();
                try {
                    await setContent({
                        uri: uri,
                        content: updateData.content
                    });
                    showToast({
                        title: i18n.t('manager.actions.page.edit-content.toast.title', "Content updated"),
                        message: i18n.t('manager.actions.page.edit-content.toast.message', "The content has been updated successfully."),
                        type: 'success', // optional: info | success | warning | error
                        timeout: 3000
                    });
                    reloadPreview();
                }
                catch (e) {
                    showToast({
                        title: i18n.t('manager.actions.page.edit-content.toast.error.title', "Content not updated"),
                        message: e.message,
                        type: 'error', // optional: info | success | warning | error
                        timeout: 3000
                    });
                }
            }
        });
    }
    catch (e) {
        showToast({
            title: i18n.t('manager.actions.page.edit-content.toast.error.title', "Content not updated"),
            message: e.message,
            type: 'error', // optional: info | success | warning | error
            timeout: 3000
        });
    }
}
