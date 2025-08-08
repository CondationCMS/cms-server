/*
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
import { i18n } from './localization.js';
const showToast = (options) => {
    const toastId = 'toast_' + Date.now();
    // Fallbacks
    const title = options.title || i18n.t("toast.title", "Note");
    const message = options.message || '';
    const type = options.type || 'info'; // info, success, warning, error
    const timeout = typeof options.timeout === 'number' ? options.timeout : 5000;
    // Toast-Container erstellen, falls nicht vorhanden
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(container);
    }
    const colorClasses = {
        info: 'bg-info text-white',
        success: 'bg-success text-white',
        warning: 'bg-warning text-dark',
        error: 'bg-danger text-white'
    };
    const toastHtml = `
		<div id="${toastId}" class="toast align-items-center ${colorClasses[type]} border-0 mb-2" role="alert" aria-live="assertive" aria-atomic="true">
		  <div class="d-flex">
			<div class="toast-body">
			  <strong>${title}</strong><br>${message}
			</div>
			<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
		  </div>
		</div>`;
    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const toastInstance = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: timeout
    });
    toastInstance.show();
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
        if (typeof options.onClose === 'function') {
            options.onClose();
        }
    });
};
export { showToast };
