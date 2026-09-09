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
import { getActivePreviewContent } from '@cms/modules/preview-context.js';
import { getContentNode } from '@cms/modules/rpc/rpc-content.js';
import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';

interface UsageResource { site: string; kind: string; path: string; }
interface Usage {
    source: UsageResource;
    sourceTitle: string;
    sourceStatus: string;
    location: string;
    originalReference: string;
}
interface UsageResult {
    items: Usage[];
    problems: { site: string; path: string; message: string }[];
}

const t = (key: string, fallback: string) => i18n.t(`usage.${key}`, fallback);
const appendText = (parent: HTMLElement, tag: string, text: string, className = '') => {
    const element = document.createElement(tag);
    element.textContent = text;
    element.className = className;
    parent.appendChild(element);
    return element;
};

const render = (container: HTMLElement, path: string, result: UsageResult) => {
    container.replaceChildren();
    appendText(container, 'p', path, 'fw-semibold text-break');
    appendText(container, 'p', t('scope',
        'Shows direct editorial references. Templates, themes and dynamic queries are not included.'),
        'text-body-secondary');
    if (result.problems.length) {
        const warning = appendText(container, 'div', t('incomplete',
            'Some sources could not be fully checked. This list may be incomplete.'), 'alert alert-warning');
        warning.setAttribute('role', 'status');
        const details = document.createElement('details');
        appendText(details, 'summary', t('problems', 'Details'));
        const list = document.createElement('ul');
        result.problems.forEach(problem => appendText(list, 'li',
            `${problem.site} / ${problem.path}: ${problem.message}`, 'text-break'));
        details.appendChild(list);
        warning.appendChild(details);
    }
    if (!result.items.length) {
        appendText(container, 'p', t('empty', 'No usages found in editorial content.'), 'mb-0');
        return;
    }
    appendText(container, 'p', `${t('count', 'Usages')}: ${result.items.length}`);
    const wrapper = document.createElement('div');
    wrapper.className = 'table-responsive';
    const table = document.createElement('table');
    table.className = 'table table-striped align-middle';
    const headings = table.createTHead().insertRow();
    [t('source', 'Source'), t('site', 'Site'), t('location', 'Location'), t('status', 'Source status')]
        .forEach(label => {
            const th = appendText(headings, 'th', label);
            th.setAttribute('scope', 'col');
        });
    const body = table.createTBody();
    result.items.forEach(usage => {
        const row = body.insertRow();
        const source = row.insertCell();
        appendText(source, 'div', usage.sourceTitle || usage.source.path, 'fw-semibold text-break');
        appendText(source, 'small', usage.source.path, 'text-body-secondary text-break');
        row.insertCell().textContent = usage.source.site;
        const location = row.insertCell();
        appendText(location, 'div', usage.location, 'text-break');
        appendText(location, 'small', usage.originalReference, 'text-body-secondary text-break');
        row.insertCell().textContent = usage.sourceStatus || '—';
    });
    wrapper.appendChild(table);
    container.appendChild(wrapper);
};

export const runAction = () => {
    // Capture the node before opening the dialog or waiting for a request.
    const url = getPreviewUrl();
    const activeNode = getActivePreviewContent(url);
    openModal({
        title: t('title', 'Show usages'),
        size: 'lg',
        showFooter: false,
        body: '<div data-usage-content aria-live="polite"></div>',
        onShow: async (modalElement: HTMLElement) => {
            const container = modalElement.querySelector('[data-usage-content]') as HTMLElement;
            appendText(container, 'p', t('loading', 'Loading usages…'));
            try {
                const node = activeNode || (url ? (await getContentNode({ url })).result : null);
                if (!node?.uri) {
                    container.replaceChildren();
                    appendText(container, 'p', t('noNode', 'No content node is selected.'));
                    return;
                }
                const response = await executeRemoteCall({
                    method: 'usage.incoming',
                    parameters: {
                        kind: node.contentKind === 'collection' ? 'COLLECTION_ITEM' : 'CONTENT',
                        path: node.uri
                    }
                });
                render(container, node.uri, response.result as UsageResult);
            } catch (error) {
                container.replaceChildren();
                const message = appendText(container, 'div',
                    t('error', 'Could not load usages.'), 'alert alert-danger');
                message.setAttribute('role', 'alert');
            }
        }
    });
};
