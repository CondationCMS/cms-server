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

import { i18n } from "./localization.js";

const defaultOptions = {
	validate: () => true
};

const openModal = (optionsParam) => {
	const modalId = 'fullscreenModal_' + Date.now();

	const options = {
		...defaultOptions,
		...optionsParam
	};

	let fullscreen = "";
	if (options.fullscreen) {
		fullscreen = "modal-fullscreen";
	}
	
	let size = ""
	if (options.size) {
		size = "modal-" + options.size
	}

	const modalHtml = `
		<div class="modal fade" id="${modalId}" tabindex="-1" aria-hidden="true">
		  <div class="modal-dialog ${fullscreen} ${size}">
			<div class="modal-content">
			  <div class="modal-header">
				<h5 class="modal-title">${options.title || 'Modal Title'}</h5>
				<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			  </div>
			  <div class="modal-body" id="${modalId}_bodyContainer">
				${options.body || ''}
			  </div>
			  <div class="modal-footer">
				<button type="button" class="btn btn-secondary" id="${modalId}_cancelBtn">${i18n.t("buttons.cancel", "Cancel")}</button>
				<button type="button" class="btn btn-primary" id="${modalId}_okBtn">${i18n.t("buttons.ok", "Ok")}</button>
			  </div>
			</div>
		  </div>
		</div>`;

	const container = document.getElementById('modalContainer');
	const modalDiv = document.createElement('div');
	modalDiv.innerHTML = modalHtml.trim();
	const modalNode = modalDiv.firstChild;
	container.appendChild(modalNode);

	const modalElement = document.getElementById(modalId);
	
	// Prüfe ob eine Offcanvas offen ist
	const openOffcanvas = document.querySelector('.offcanvas.show');
	const hasOpenOffcanvas = openOffcanvas !== null;
	
	// Z-Index höher setzen wenn Offcanvas offen ist
	const modalZIndex = hasOpenOffcanvas ? 1080 : 1060;
	const backdropZIndex = hasOpenOffcanvas ? 1070 : 1055;
	
	modalElement.style.zIndex = modalZIndex;
	modalElement.style.pointerEvents = 'auto';
	
	const modalInstance = new bootstrap.Modal(modalElement, {
		backdrop: 'static',
		keyboard: true,
		focus: true
	});

	modalElement.addEventListener('shown.bs.modal', function (event) {
		// Backdrop z-index anpassen
		const backdrops = document.querySelectorAll('.modal-backdrop');
		const latestBackdrop = backdrops[backdrops.length - 1];
		if (latestBackdrop) {
			latestBackdrop.style.zIndex = backdropZIndex;
			latestBackdrop.style.pointerEvents = 'none';
		}
		
		// Offcanvas temporär nach hinten schieben
		if (hasOpenOffcanvas) {
			openOffcanvas.style.zIndex = '1050';
			openOffcanvas.style.pointerEvents = 'none';
			const offcanvasBackdrop = document.querySelector('.offcanvas-backdrop');
			if (offcanvasBackdrop) {
				offcanvasBackdrop.style.zIndex = '1045';
				offcanvasBackdrop.style.pointerEvents = 'none';
			}
		}
		
		// Modal und Content explizit aktivieren
		modalElement.style.pointerEvents = 'auto';
		const modalContent = modalElement.querySelector('.modal-content');
		if (modalContent) {
			modalContent.style.pointerEvents = 'auto';
		}
		
		// Form ERST initialisieren wenn Modal sichtbar ist
		if (options.form) {
			options.form.init(`#${modalId}_bodyContainer`);
		}
		
		if (options.onShow) {
			options.onShow(modalElement);
		}
	});

	modalInstance.show();

	// Event-Handler
	document.getElementById(`${modalId}_cancelBtn`).addEventListener('click', () => {
		modalInstance.hide();
		if (typeof options.onCancel === 'function')
			options.onCancel();
	});

	document.getElementById(`${modalId}_okBtn`).addEventListener('click', () => {
		if (options.validate()) {
			modalInstance.hide();
			if (typeof options.onOk === 'function')
				options.onOk();
		}
	});

	// Clean-up nach Schließen
	modalElement.addEventListener('hidden.bs.modal', () => {
		// Offcanvas z-index und pointer-events wiederherstellen
		if (hasOpenOffcanvas && openOffcanvas) {
			openOffcanvas.style.zIndex = '';
			openOffcanvas.style.pointerEvents = '';
			const offcanvasBackdrop = document.querySelector('.offcanvas-backdrop');
			if (offcanvasBackdrop) {
				offcanvasBackdrop.style.zIndex = '';
				offcanvasBackdrop.style.pointerEvents = '';
			}
		}
		
		modalNode.remove();
		if (options.onClose) {
			options.onClose();
		}
	});

	return modalInstance;
};

export { openModal };