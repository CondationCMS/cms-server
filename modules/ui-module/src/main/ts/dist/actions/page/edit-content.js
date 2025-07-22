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
import { openModal } from '../../js/modules/modal.js';
import { createForm } from '../../js/modules/form/forms.js';
import { getPreviewUrl, reloadPreview } from '../../js/modules/preview.utils.js';
import { getContentNode, getContent, setContent } from '../../js/modules/rpc/rpc-content.js';
import { i18n } from '../../js/modules/localization.js';
import { showToast } from '../../js/modules/toast.js';
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
            var setContentResponse = await setContent({
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
    });
}
