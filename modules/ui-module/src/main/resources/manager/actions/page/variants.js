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
import { openModal } from '@cms/modules/modal.js';
import { getPreviewUrl, loadPreview } from '@cms/modules/preview.utils.js';
import { getContentNode } from '@cms/modules/rpc/rpc-content.js';
import { deleteVariant, getVariants } from '@cms/modules/rpc/rpc-variant.js';
import { showToast } from '@cms/modules/toast.js';
import { ensureVariantsSupported } from '@cms/modules/variant-support.js';
const VARIANT_LIST_ID = 'cms-page-variants';
const variantTitle = (variant) => {
    const title = variant.meta?.title;
    return typeof title === 'string' && title.trim() ? title : variant.id;
};
const createVariantLink = (titleText, idText, uriText, url, variantId, active, modal) => {
    const link = document.createElement('a');
    link.href = url;
    link.className = `list-group-item list-group-item-action${active ? ' active' : ''}`;
    link.setAttribute('aria-current', active ? 'true' : 'false');
    const heading = document.createElement('div');
    heading.className = 'd-flex w-100 justify-content-between align-items-center gap-3';
    const title = document.createElement('strong');
    title.textContent = titleText;
    heading.appendChild(title);
    const id = document.createElement('span');
    id.className = active ? 'badge text-bg-light' : 'badge text-bg-secondary';
    id.textContent = idText;
    heading.appendChild(id);
    link.appendChild(heading);
    const uri = document.createElement('small');
    uri.className = active ? '' : 'text-body-secondary';
    uri.textContent = uriText;
    link.appendChild(uri);
    link.addEventListener('click', (event) => {
        event.preventDefault();
        modal.hide();
        loadPreview(url, variantId ? { variant: variantId } : {});
    });
    return link;
};
const createVariantItem = (variant, result, modal, container) => {
    const item = document.createElement('div');
    const active = result.activeVariantId === variant.id;
    item.className = `list-group-item d-flex align-items-center gap-2 p-0${active ? ' active' : ''}`;
    const link = createVariantLink(variantTitle(variant), variant.id, variant.uri, result.canonical.url, variant.id, active, modal);
    link.classList.remove('list-group-item', 'active');
    link.classList.add('flex-grow-1', 'border-0', 'rounded-0', 'px-3', 'py-2');
    if (active) {
        link.classList.add('text-reset');
    }
    item.appendChild(link);
    const deleteButton = document.createElement('button');
    deleteButton.type = 'button';
    deleteButton.className = 'btn btn-sm btn-outline-danger flex-shrink-0 me-2';
    deleteButton.title = i18n.t('manager.actions.page.variants.delete', 'Delete variant');
    deleteButton.setAttribute('aria-label', `${deleteButton.title}: ${variant.id}`);
    deleteButton.innerHTML = '<i class="bi bi-trash" aria-hidden="true"></i>';
    deleteButton.addEventListener('click', async () => {
        const confirmed = await alertConfirm({
            title: i18n.t('manager.actions.page.variants.delete.confirm.title', 'Delete page variant?'),
            message: i18n.t('manager.actions.page.variants.delete.confirm.message', `The variant “${escapeHtml(variantTitle(variant))}” and all its sections will be permanently deleted.`)
        });
        if (!confirmed) {
            return;
        }
        try {
            const wasActive = result.activeVariantId === variant.id;
            const deleted = await deleteVariant(result.canonical.uri, variant.id);
            showToast({
                title: i18n.t('manager.actions.page.variants.delete.success.title', 'Variant deleted'),
                message: i18n.t('manager.actions.page.variants.delete.success.message', `The variant “${variant.id}” was deleted.`),
                type: 'success',
                timeout: 3000
            });
            if (wasActive) {
                modal.hide();
                loadPreview(deleted.url);
                return;
            }
            result.variants = result.variants.filter(candidate => candidate.id !== variant.id);
            container.replaceChildren();
            renderVariants(container, result, modal);
        }
        catch (error) {
            showToast({
                title: i18n.t('manager.actions.page.variants.delete.error.title', 'Could not delete variant'),
                message: error instanceof Error ? error.message : String(error),
                type: 'error',
                timeout: 3000
            });
        }
    });
    item.appendChild(deleteButton);
    return item;
};
const renderVariants = (container, result, modal) => {
    if (result.variants.length === 0) {
        const emptyMessage = document.createElement('p');
        emptyMessage.className = 'text-body-secondary mb-0';
        emptyMessage.textContent = i18n.t('manager.actions.page.variants.empty', 'This page has no variants.');
        container.appendChild(emptyMessage);
        return;
    }
    const list = document.createElement('div');
    list.className = 'list-group';
    list.appendChild(createVariantLink(result.canonical.title, i18n.t('manager.actions.page.variants.canonical', 'Original'), result.canonical.uri, result.canonical.url, null, !result.activeVariantId, modal));
    result.variants.forEach((variant) => {
        list.appendChild(createVariantItem(variant, result, modal, container));
    });
    container.appendChild(list);
};
const escapeHtml = (input) => {
    const element = document.createElement('div');
    element.textContent = String(input ?? '');
    return element.innerHTML;
};
export const runAction = async () => {
    try {
        const contentNode = await getContentNode({
            url: getPreviewUrl()
        });
        if (!ensureVariantsSupported(contentNode.result)) {
            return;
        }
        const result = await getVariants({
            uri: contentNode.result.uri
        });
        let modal;
        modal = openModal({
            title: i18n.t('manager.actions.page.variants.title', 'Page variants'),
            body: `<div id="${VARIANT_LIST_ID}"></div>`,
            fullscreen: false,
            size: 'lg',
            onCancel: () => { },
            onOk: () => { },
            onShow: (modalElement) => {
                const container = modalElement.querySelector(`#${VARIANT_LIST_ID}`);
                renderVariants(container, result, modal);
            }
        });
    }
    catch (error) {
        showToast({
            title: i18n.t('manager.actions.page.variants.error.title', 'Could not load page variants'),
            message: error instanceof Error ? error.message : String(error),
            type: 'error',
            timeout: 3000
        });
    }
};
