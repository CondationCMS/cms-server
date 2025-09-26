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

import frameMessenger from './modules/frameMessenger.js';

import { initContentMediaToolbar, initMediaToolbar, initMediaUploadOverlay } from './modules/manager/media.inject.js';
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_PUBLISHED_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from './modules/manager/toolbar-icons';
import { initToolbar } from './modules/manager/toolbar.inject.js';

const isIframe = () => {
	return typeof window !== 'undefined' && window.self !== window.top;
}

document.addEventListener("DOMContentLoaded", function () {

	if (!isIframe()) {
		return;
	}

	frameMessenger.send(window.parent, {
		type: 'loaded',
		payload: {}
	});

	const toolbarContainers = document.querySelectorAll('[data-cms-toolbar]');
	toolbarContainers.forEach(initToolbar);

	const mediaToolbarContainers = document.querySelectorAll('img[data-cms-media-toolbar]');
	mediaToolbarContainers.forEach(initMediaToolbar);

	const contentMediaContainers = document.querySelectorAll('img[data-cms-ui-selector=content-image]');
	contentMediaContainers.forEach(initContentMediaToolbar);

	//const mediaUploadContainers = document.querySelectorAll('img[data-cms-media-actions~=upload]');
	//mediaUploadContainers.forEach(initMediaUploadOverlay);

	document.addEventListener('keydown', (event) => {
		if (event.key.toLowerCase() === 'k' && (event.metaKey || event.ctrlKey)) {
			event.preventDefault();

			frameMessenger.send(window.parent, {
				type: 'shortkeys',
				payload: {}
			});
		}
	})

	frameMessenger.on('getContentNodeResponse', async (payload) => {
		for (const [sectionName, items] of Object.entries(payload.contentNode.sections)) {

			for (const item of items) {
				const sectionContainer = document.querySelector(`[data-cms-section-uri="${item.uri}"]`);
				if (!sectionContainer) {
					continue;
				}
				if (item.data.published) {
					sectionContainer.setAttribute('data-cms-action', 'unpublish');
					sectionContainer.setAttribute("title", "Unpublish");
					if (isSectionPublishedExpired(item)) {

						sectionContainer.innerHTML = SECTION_UNPUBLISHED_ICON;

						sectionContainer.classList.remove('cms-unpublished');
						sectionContainer.classList.remove('cms-published');
						sectionContainer.classList.add('cms-published-expired');
					} else {

						sectionContainer.innerHTML = SECTION_PUBLISHED_ICON;

						sectionContainer.classList.remove('cms-unpublished');
						sectionContainer.classList.remove('cms-published-expired');
						sectionContainer.classList.add('cms-published');
					}
				} else {
					sectionContainer.innerHTML = SECTION_UNPUBLISHED_ICON;
					sectionContainer.setAttribute('data-cms-action', 'publish');
					sectionContainer.setAttribute("title", "Publish");
					sectionContainer.classList.remove('cms-published');
					sectionContainer.classList.remove('cms-published-expired');
					sectionContainer.classList.add('cms-unpublished');
				}
			}
		}
	});

	frameMessenger.send(window.parent, {
		type: 'getContentNode',
		payload: {}
	});

	document.addEventListener('click', function (event) {
		if (event.target.matches('[data-cms-action]')) {
			const button = event.target;

			// Wenn bereits disabled, nichts tun
			if (button.disabled) {
				event.preventDefault();
				return;
			}

			// Button deaktivieren
			button.disabled = true;

			// Nach 2 Sekunden wieder aktivieren
			setTimeout(() => {
				button.disabled = false;
			}, 2000);
		}
	});
});

export function isSectionPublishedExpired(section) {
	const publishDateStr = section.data.publish_date;
	const unpublishDateStr = section.data.unpublish_date;

	const now = new Date();

	const publishDate = publishDateStr ? new Date(publishDateStr) : null;
	const unpublishDate = unpublishDateStr ? new Date(unpublishDateStr) : null;

	// section is published if:
	// - publishDate empty or in the past
	// - und unpublishDate empty or in the future
	const isPublished =
		(!publishDate || publishDate <= now) &&
		(!unpublishDate || unpublishDate > now);

	return !isPublished;
}