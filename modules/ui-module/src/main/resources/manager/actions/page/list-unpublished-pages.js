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
import { filterPages } from '@cms/modules/rpc/rpc-page';
import { loadPreview } from '@cms/modules/preview.utils';
const ITEMS_PER_PAGE = 5; // Feste Seitengröße wie angefordert
const renderPageListHtml = (pages, currentPage, totalPages) => {
    let pageItemsHtml = '';
    if (pages.length === 0) {
        pageItemsHtml = `<p>${i18n.t('page.unpublished.noPages', 'No unpublished pages found.')}</p>`;
    }
    else {
        pageItemsHtml = `
            <ul class="list-group">
                ${pages.map(page => `
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${page.meta?.title}</strong>
                            <br>
                            <small>${page.uri}</small>
                        </div>
                        <a data-cms-page-uri="${page.uri}" class="btn btn-sm btn-outline-primary" target="_blank">
                            ${i18n.t('page.unpublished.editLink', 'Edit')}
                        </a>
                    </li>
                `).join('')}
            </ul>
        `;
    }
    const paginationHtml = `
        <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center mt-3">
                <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${currentPage - 1}">${i18n.t('pagination.previous', 'Previous')}</a>
                </li>
                <li class="page-item disabled">
                    <span class="page-link">${currentPage} / ${totalPages}</span>
                </li>
                <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${currentPage + 1}">${i18n.t('pagination.next', 'Next')}</a>
                </li>
            </ul>
        </nav>
    `;
    return `
        <div>
            ${pageItemsHtml}
            ${paginationHtml}
        </div>
    `;
};
const state = {
    modal: null
};
const updateDialog = async (pageNumber) => {
    const filterOptions = {
        where: [
            {
                field: "status",
                operator: "=",
                value: "draft"
            }
        ],
        page: pageNumber,
        size: ITEMS_PER_PAGE
    };
    try {
        const response = await filterPages(filterOptions);
        var pageData = response.result;
        const modalBodyHtml = renderPageListHtml(pageData.items, pageData.page, pageData.totalPages);
        var modalElement = document.getElementById('cms-unpublished-pages-modal-body');
        if (modalElement) {
            modalElement.innerHTML = modalBodyHtml;
            modalElement.querySelectorAll('.page-link').forEach(link => {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    const newPage = parseInt(e.target.dataset.page || '1');
                    if (newPage >= 1 && newPage <= pageData.totalPages) {
                        updateDialog(newPage);
                    }
                });
            });
            modalElement.querySelectorAll('a[data-cms-page-uri]').forEach((link) => {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    state.modal.hide();
                    loadPreview(link.dataset.cmsPageUri || '');
                });
            });
        }
    }
    catch (e) {
        var modalElement = document.getElementById('cms-unpublished-pages-modal-body');
        if (modalElement) {
            modalElement.innerHTML = `<p>${i18n.t('page.unpublished.loadError', 'Could not load unpublished pages.')}</p>`;
        }
    }
};
export const runAction = async (options = {}) => {
    let currentPage = options.page || 1;
    state.modal = openModal({
        title: i18n.t('page.unpublished.title', 'Unpublished Pages'),
        body: "<div id='cms-unpublished-pages-modal-body'></div>",
        fullscreen: false,
        onCancel: () => { },
        onOk: () => { },
        onShow: (modalElement) => {
            updateDialog(currentPage);
        }
    });
};
