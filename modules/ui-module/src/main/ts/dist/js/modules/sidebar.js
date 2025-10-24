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
        : 'end';
    const isResizable = position === 'end' && (options.resizable === true);
    const sidebarHtml = `
		<div class="offcanvas offcanvas-${position} ${isResizable ? 'resizable' : ''}" tabindex="-1" id="${sidebarId}" aria-labelledby="${sidebarId}_label">
			${isResizable ? '<div class="cms-resize-handle"></div>' : ''}
			<div class="offcanvas-header">
				<h5 class="offcanvas-title" id="${sidebarId}_label">${options.title || 'Sidebar Title'}</h5>
				<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
			</div>
			<div class="offcanvas-body" id="sidebarBodyContainer">
				${options.body || '<p>Sidebar content</p>'}
			</div>
			<div class="offcanvas-footer d-flex justify-content-center gap-2 p-2">
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
    // Setze initiale Breite nur für resizable sidebar
    if (isResizable) {
        sidebarElement.style.width = (options.initialWidth || 400) + 'px';
    }
    const sidebarInstance = new bootstrap.Offcanvas(sidebarElement, {
        backdrop: 'static',
        keyboard: options.keyboard ?? false
    });
    sidebarInstance.show();
    // Fix für Modals über Offcanvas
    const handleModalShow = (e) => {
        const modal = e.target;
        const modalBackdrop = document.querySelector('.modal-backdrop:last-of-type');
        // Setze höhere z-index Werte
        modal.style.zIndex = '1080';
        if (modalBackdrop) {
            modalBackdrop.style.zIndex = '1070';
        }
        // Reduziere Offcanvas z-index temporär
        sidebarElement.style.zIndex = '1050';
        const offcanvasBackdrop = document.querySelector('.offcanvas-backdrop');
        if (offcanvasBackdrop) {
            offcanvasBackdrop.style.zIndex = '1045';
        }
    };
    const handleModalHide = () => {
        // Stelle Offcanvas z-index wieder her
        sidebarElement.style.zIndex = '';
        const offcanvasBackdrop = document.querySelector('.offcanvas-backdrop');
        if (offcanvasBackdrop) {
            offcanvasBackdrop.style.zIndex = '';
        }
    };
    // Event Listener für alle Modals
    document.addEventListener('show.bs.modal', handleModalShow);
    document.addEventListener('hidden.bs.modal', handleModalHide);
    // Buttons
    document.getElementById(`${sidebarId}_cancelBtn`).addEventListener('click', () => {
        sidebarInstance.hide();
        if (typeof options.onCancel === 'function')
            options.onCancel();
    });
    document.getElementById(`${sidebarId}_okBtn`).addEventListener('click', () => {
        sidebarInstance.hide();
        if (typeof options.onOk === 'function')
            options.onOk();
    });
    // Resize-Funktion (nur bei "end"-Position UND resizable === true)
    let mouseMoveHandler, mouseUpHandler;
    if (isResizable) {
        const handle = sidebarElement.querySelector('.cms-resize-handle');
        let isResizing = false;
        const mouseDownHandler = (e) => {
            isResizing = true;
            sidebarElement.classList.add('is-resizing');
            document.body.classList.add('resizing-sidebar');
        };
        mouseMoveHandler = (e) => {
            if (!isResizing)
                return;
            const newWidth = window.innerWidth - e.clientX;
            const clampedWidth = Math.min(Math.max(newWidth, 250), window.innerWidth * 0.9);
            sidebarElement.style.width = clampedWidth + 'px';
        };
        mouseUpHandler = () => {
            if (isResizing) {
                isResizing = false;
                sidebarElement.classList.remove('is-resizing');
                document.body.classList.remove('resizing-sidebar');
            }
        };
        handle.addEventListener('mousedown', mouseDownHandler);
        document.addEventListener('mousemove', mouseMoveHandler);
        document.addEventListener('mouseup', mouseUpHandler);
    }
    // Cleanup
    sidebarElement.addEventListener('hidden.bs.offcanvas', () => {
        // Entferne Modal Event-Listener
        document.removeEventListener('show.bs.modal', handleModalShow);
        document.removeEventListener('hidden.bs.modal', handleModalHide);
        // Entferne Resize Event-Listener
        if (mouseMoveHandler)
            document.removeEventListener('mousemove', mouseMoveHandler);
        if (mouseUpHandler)
            document.removeEventListener('mouseup', mouseUpHandler);
        // Cleanup body classes
        document.body.classList.remove('resizing-sidebar');
        // Cleanup DOM
        container.innerHTML = '';
    });
};
export { openSidebar };
