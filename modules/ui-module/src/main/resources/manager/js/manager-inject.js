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

import frameMessenger from '/manager/js/modules/frameMessenger.js';

document.addEventListener("DOMContentLoaded", function () {
	frameMessenger.on('init', (payload) => {
		console.log('Parent sagt:', payload.msg);

		frameMessenger.send(window.parent, {
			type: 'helloFromIframe',
			payload: {response: 'Hallo Parent!'}
		});
	});

	const containers = document.querySelectorAll('[data-cms-editor]');

	containers.forEach(container => {
		const toolbar = container.querySelector('.toolbar');

		container.addEventListener('mouseover', () => {
			toolbar.classList.add('visible');
		});

		container.addEventListener('mouseleave', (event) => {
			if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
				toolbar.classList.remove('visible');
			}
		});

		toolbar.addEventListener('mouseleave', (event) => {
			if (!event.relatedTarget || !container.contains(event.relatedTarget)) {
				toolbar.classList.remove('visible');
			}
		});

		toolbar.querySelectorAll("button").forEach(button => {
			button.addEventListener('click', edit);
		})
	});
});

const edit = (event) => {
	console.log(event)
	var $editor = event.target.closest('[data-cms-editor]');
	if ($editor) {
		var contentUri = event.target.closest('[data-cms-content-uri]')
		
		var command = {
			type: 'edit',
			payload: {
				editor: $editor.dataset.cmsEditor,
				element: $editor.dataset.cmsElement
			}
		}
		if (contentUri) {
			command.payload.uri = contentUri.dataset.cmsContentUri
		}
		
		console.log(command)
		
		frameMessenger.send(window.parent, command);
	}
}