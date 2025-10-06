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

	if (options.form) {
		options.form.init(`#${modalId}_bodyContainer`)
	}

	const modalElement = document.getElementById(modalId);
	
	// Z-Index setzen BEVOR Modal initialisiert wird
	modalElement.style.zIndex = '1060';
	
	const modalInstance = new bootstrap.Modal(modalElement, {
		backdrop: 'static', // Wichtig: static statt false
		keyboard: true,
		focus: false
	});

	modalElement.addEventListener('shown.bs.modal', function (event) {
		// Backdrop z-index anpassen
		const backdrops = document.querySelectorAll('.modal-backdrop');
		backdrops.forEach(backdrop => {
			backdrop.style.zIndex = '1055';
		});
		
		if (options.onShow) {
			options.onShow()
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
		modalNode.remove();
		if (options.onClose) {
			options.onClose()
		}
	});

	return modalInstance
};

export { openModal };