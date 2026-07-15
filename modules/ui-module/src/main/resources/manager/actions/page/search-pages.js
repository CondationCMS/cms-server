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
import { i18n } from '@cms/modules/localization.js';
import { searchPages } from '@cms/modules/rpc/rpc-page';
import { loadPreview } from '@cms/modules/preview.utils';
const MIN_QUERY_LENGTH = 3;
const renderResultsHtml = (results, query) => {
    if (query.trim().length < MIN_QUERY_LENGTH) {
        return `<p>${i18n.t('page.search.minLength', 'Enter at least 3 characters and press Enter to search.')}</p>`;
    }
    if (results.length === 0) {
        return `<p>${i18n.t('page.search.noResults', 'No pages found.')}</p>`;
    }
    return `
        <table class="table">
            <thead>
                <tr>
                    <th>${i18n.t('page.search.columnTitle', 'Title')}</th>
                    <th>${i18n.t('page.search.columnUri', 'URI')}</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                ${results.map(result => `
                    <tr>
                        <td>${result.title}</td>
                        <td>${result.uri}</td>
                        <td>
                            <a data-cms-page-uri="${result.uri}" class="btn btn-sm btn-outline-primary" href="#">
                                ${i18n.t('page.search.loadLink', 'Load')}
                            </a>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
};
const state = {
    modal: null
};
const bindResultLinks = (resultsElement) => {
    resultsElement.querySelectorAll('a[data-cms-page-uri]').forEach((link) => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            state.modal.hide();
            loadPreview(link.dataset.cmsPageUri || '');
        });
    });
};
const runSearch = async (query, resultsElement) => {
    if (query.trim().length < MIN_QUERY_LENGTH) {
        resultsElement.innerHTML = renderResultsHtml([], query);
        return;
    }
    try {
        const response = await searchPages({ query: query.trim() });
        resultsElement.innerHTML = renderResultsHtml(response.result, query);
        bindResultLinks(resultsElement);
    }
    catch (e) {
        resultsElement.innerHTML = `<p>${i18n.t('page.search.loadError', 'Could not search pages.')}</p>`;
    }
};
export const runAction = async (options = {}) => {
    state.modal = openModal({
        title: i18n.t('page.search.title', 'Search pages'),
        body: `
            <input type="search" class="form-control" id="cms-search-pages-input"
                placeholder="${i18n.t('page.search.placeholder', 'Search by title...')}" />
            <div id="cms-search-pages-results" class="mt-3"></div>
        `,
        fullscreen: false,
        onCancel: () => { },
        onOk: () => { },
        onShow: (modalElement) => {
            const inputElement = modalElement.querySelector('#cms-search-pages-input');
            const resultsElement = modalElement.querySelector('#cms-search-pages-results');
            resultsElement.innerHTML = renderResultsHtml([], '');
            inputElement.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    runSearch(inputElement.value, resultsElement);
                }
            });
            inputElement.focus();
        }
    });
};
