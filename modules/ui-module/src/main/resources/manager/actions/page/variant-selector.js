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
import { openModal } from '@cms/modules/modal.js';
import { getPreviewUrl } from '@cms/modules/preview.utils.js';
import { getContentNode } from '@cms/modules/rpc/rpc-content.js';
import { getVariantSelectors, setVariantSelector } from '@cms/modules/rpc/rpc-variant.js';
import { showToast } from '@cms/modules/toast.js';
const SELECT_ID = 'cms-variant-selector';
const escapeHtml = (input) => {
    const element = document.createElement('div');
    element.textContent = String(input ?? '');
    return element.innerHTML;
};
const option = (selector, selected) => `<option value="${escapeHtml(selector.id)}"${selector.id === selected ? ' selected' : ''}>`
    + `${escapeHtml(selector.label)}</option>`;
const showError = (error) => showToast({
    title: i18n.t('manager.actions.page.variant-selector.error.title', 'Could not configure variant selection'),
    message: error instanceof Error ? error.message : String(error),
    type: 'error',
    timeout: 3000
});
export const runAction = async () => {
    try {
        const contentNode = await getContentNode({ url: getPreviewUrl() });
        const result = await getVariantSelectors(contentNode.result.uri);
        openModal({
            title: i18n.t('manager.actions.page.variant-selector.title', 'Configure variant selection'),
            body: `
				<div class="mb-3">
					<label for="${SELECT_ID}" class="form-label">Selection strategy</label>
					<select id="${SELECT_ID}" class="form-select">
						${result.selectors.map(selector => option(selector, result.selector)).join('')}
					</select>
					<div class="form-text">
						The strategy is applied to public requests. Manager and explicit preview selection remain unchanged.
					</div>
				</div>`,
            fullscreen: false,
            onCancel: () => { },
            validate: () => Boolean(document.getElementById(SELECT_ID)?.value),
            onOk: async () => {
                try {
                    const selector = document.getElementById(SELECT_ID).value;
                    await setVariantSelector(contentNode.result.uri, selector);
                    showToast({
                        title: i18n.t('manager.actions.page.variant-selector.success.title', 'Variant selection configured'),
                        message: i18n.t('manager.actions.page.variant-selector.success.message', 'The selection strategy was saved.'),
                        type: 'success',
                        timeout: 3000
                    });
                }
                catch (error) {
                    showError(error);
                }
            }
        });
    }
    catch (error) {
        showError(error);
    }
};
