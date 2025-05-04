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

const openModal = (options) => {
	const modalId = 'fullscreenModal_' + Date.now();

	const modalHtml = `
		<div class="modal fade" id="${modalId}" tabindex="-1" aria-hidden="true">
		  <div class="modal-dialog modal-fullscreen">
			<div class="modal-content">
			  <div class="modal-header">
				<h5 class="modal-title">${options.title || 'Modal Title'}</h5>
				<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
			  </div>
			  <div class="modal-body">
				${options.body || '<p>Modal body content</p>'}
			  </div>
			  <div class="modal-footer">
				<button type="button" class="btn btn-secondary" id="${modalId}_cancelBtn">Cancel</button>
				<button type="button" class="btn btn-primary" id="${modalId}_okBtn">OK</button>
			  </div>
			</div>
		  </div>
		</div>`;

	// Modal einfügen
	const container = document.getElementById('modalContainer');
	container.innerHTML = modalHtml;

	const modalElement = document.getElementById(modalId);
	const modalInstance = new bootstrap.Modal(modalElement);
	modalInstance.show();

	// Event-Handler
	document.getElementById(`${modalId}_cancelBtn`).addEventListener('click', () => {
		modalInstance.hide();
		if (typeof options.onCancel === 'function')
			options.onCancel();
	});

	document.getElementById(`${modalId}_okBtn`).addEventListener('click', () => {
		modalInstance.hide();
		if (typeof options.onOk === 'function')
			options.onOk();
	});

	// Clean-up nach Schließen
	modalElement.addEventListener('hidden.bs.modal', () => {
		container.innerHTML = '';
	});
};

export {openModal};
