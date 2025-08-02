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

import { i18n } from "../../js/modules/localization.js";
import { openModal } from "../../js/modules/modal.js";
import { reloadPreview } from "../../js/modules/preview.utils.js";
import { getMediaMetaData, setMediaMetaData } from "../../js/modules/rpc/rpc-media.js";
import { showToast } from "../../js/modules/toast.js";

export async function runAction(params) {

	var uri = params.options.uri || null;
	var mediaUrl = removeFormatParamFromUrl(uri);

	const template = `
		<div class="cms-focal-wrapper" id="cmsFocalWrapper">
  			<img src="${mediaUrl}" id="cms-image"  />
  			<div class="cms-focal-point" id="cmsFocalPoint" style="display: none;"></div>
		</div>
	`;

	var mediaMeta = (await getMediaMetaData({ image: params.options.uri })).result.meta;
	const focal = mediaMeta?.focal || {};

	const focalX = typeof focal.x === 'number' ? focal.x : 0.5;
	const focalY = typeof focal.y === 'number' ? focal.y : 0.5;

	openModal({
		title: i18n.t("media.focal.title", "Edit focal point"),
		body: template,
		onCancel: (event) => { },
		onOk: async (event) => {
			var setMetaResponse = await setMediaMetaData({
				image: mediaUrl,
				meta: {
					"focal.x": {
						"type": "number",
						"value": focal.x
					},
					"focal.y": {
						"type": "number",
						"value": focal.y
					}
				}
			});
			showToast({
				title: i18n.t('manager.actions.media.focal-point.toast.title', "Media focal point updated"),
				message: i18n.t('manager.actions.media.focal-point.toast.message', "The focal point was successfuly updated."),
				type: 'success',
				timeout: 3000
			});
			reloadPreview();
		},
		onShow: () => {
			const wrapper: HTMLElement = document.getElementById("cmsFocalWrapper");
			const image: HTMLImageElement = document.getElementById("cms-image") as HTMLImageElement;
			const point: HTMLElement = document.getElementById("cmsFocalPoint");

			if (image.complete) {
				setFocalPoint(image, point, focalX, focalY);
			} else {
				image.onload = () => setFocalPoint(image, point, focalX, focalY);
			}

			wrapper.addEventListener("click", function (e) {
				const rect = image.getBoundingClientRect();
				const x = e.clientX - rect.left;
				const y = e.clientY - rect.top;

				const relX = (x / rect.width).toFixed(4);
				const relY = (y / rect.height).toFixed(4);

				// Punkt anzeigen
				point.style.left = `${x}px`;
				point.style.top = `${y}px`;
				point.style.display = "block";

				focal.x = parseFloat(relX);
				focal.y = parseFloat(relY);
				// Ausgabe
				console.log(`Focal Point: x: ${relX}, y: ${relY}`);
			});
		}
	});
}

const setFocalPoint = (image: HTMLImageElement, point: HTMLElement, relX: number, relY: number) => {
	const rect = image.getBoundingClientRect();
	const x = rect.width * relX;
	const y = rect.height * relY;

	point.style.left = `${x}px`;
	point.style.top = `${y}px`;
	point.style.display = "block";
}

const removeFormatParamFromUrl = (url: string) => {
  try {
    const parsedUrl = new URL(url, window.location.origin); // Fallback-Basis falls nur Pfad übergeben wird
    parsedUrl.searchParams.delete("format");
    return parsedUrl.toString();
  } catch (e) {
    console.warn("Ungültige URL:", url);
    return url; // Fallback: gib Original zurück
  }
}