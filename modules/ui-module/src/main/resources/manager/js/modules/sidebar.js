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
const openSidebar = (options) => {
    const sidebarId = 'offcanvasSidebar_' + Date.now();
    const position = ['start', 'end', 'top', 'bottom'].includes(options.position)
        ? options.position
        : 'end'; // default = right
    const sidebarHtml = `
		<div class="offcanvas offcanvas-${position}" tabindex="-1" id="${sidebarId}" aria-labelledby="${sidebarId}_label">
			<div class="offcanvas-header">
				<h5 class="offcanvas-title" id="${sidebarId}_label">${options.title || 'Sidebar Title'}</h5>
				<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
			</div>
			<div class="offcanvas-body" id="sidebarBodyContainer">
				${options.body || '<p>Sidebar content</p>'}
			</div>
			<div class="offcanvas-footer d-flex justify-content-center gap-2 mt-3 mb-3">
				<button type="button" class="btn btn-secondary" id="${sidebarId}_cancelBtn">Cancel</button>
				<button type="button" class="btn btn-primary" id="${sidebarId}_okBtn">OK</button>
			</div>
		</div>
	`;
    const container = document.getElementById('sidebarContainer');
    container.innerHTML = sidebarHtml;
    if (options.form) {
        options.form.init("#sidebarBodyContainer");
    }
    const sidebarElement = document.getElementById(sidebarId);
    const sidebarInstance = new bootstrap.Offcanvas(sidebarElement, {
        backdrop: options.backdrop !== undefined ? options.backdrop : false,
        keyboard: options.keyboard ?? false
    });
    sidebarInstance.show();
    document.getElementById(`${sidebarId}_cancelBtn`).addEventListener('click', () => {
        sidebarInstance.hide();
        if (typeof options.onCancel === 'function') {
            options.onCancel();
        }
    });
    document.getElementById(`${sidebarId}_okBtn`).addEventListener('click', () => {
        sidebarInstance.hide();
        if (typeof options.onOk === 'function') {
            options.onOk();
        }
    });
    sidebarElement.addEventListener('hidden.bs.offcanvas', () => {
        container.innerHTML = '';
    });
};
export { openSidebar };
