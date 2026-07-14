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
import { openModal } from '@cms/modules/modal.js'
import { showToast } from '@cms/modules/toast.js'
import { addSection, getContentNode } from '@cms/modules/rpc/rpc-content.js'
import { getPreviewUrl, reloadPreview } from '@cms/modules/preview.utils.js'
import Handlebars from 'https://cdn.jsdelivr.net/npm/handlebars@4.7.8/+esm'
import { i18n } from '@cms/modules/localization.js'
import { getSectionEntryTemplates } from '@cms/modules/rpc/rpc-manager.js';

export async function runAction(params) {

	const contentNode = await getContentNode({
		url: getPreviewUrl()
	})

	var template = Handlebars.compile(`
		<div class="mb-3">
  			<label for="cms-section-name" class="form-label">Name for the item</label>
  			<input type="text" class="form-control" id="cms-section-name" placeholder="Name of the Item">
		</div>
		<select id="cms-section-template-selection" class="form-select" aria-label="Select Item template">
			<option value="000" selected>Select template</option>
			{{#each templates}}
				<option value="{{template}}">{{name}}</option>
			{{/each}}
		</select>
		`);

		var sectionsResponse = await getSectionEntryTemplates({
			section: params.section
		});

	openModal({
		title: i18n.t("addsection.titles.modal", 'Add entry'),
		body: template({ 
			templates: sectionsResponse.result,
			
		}),
		fullscreen: false,
		onCancel: (event) => {},
		validate: () => validate(contentNode, params.section),
		onOk: async (event) => {
			var result = await createSection(contentNode.result.uri, params.section);
			if (result) {
				showToast({
					title: i18n.t("manager.actions.addsection.titles.alert", "Create Entry"),
					message: i18n.t("manager.actions.addsection.alerts.success.message", "Entry successfuly created."),
					type: 'success', // optional: info | success | warning | error
					timeout: 3000
				});
				await new Promise(resolve => setTimeout(resolve, 1000));
				reloadPreview()
			} else {
				showToast({
					title: i18n.t("manager.actions.addsection.titles.alert", 'Create Entry'),
					message: i18n.t("manager.actions.addsection.alerts.error.message", "Entry not created."),
					type: 'warning', // optional: info | success | warning | error
					timeout: 3000
				});
			}

		}
	});
}

const getSectionEntryName = () => {
	return document.getElementById("cms-section-name").value
}

const validate = (contentNode, targetSection) => {
	const template = document.getElementById("cms-section-template-selection").value
	if (template === "000") {
		showToast({
			title: i18n.t("manager.actions.addsection.titles.alert", 'Create Entry'),
			message: i18n.t("manager.actions.addsection.alerts.notemplate.message", "No template selected."),
			type: 'error', // optional: info | success | warning | error
			timeout: 3000
		});
		return false
	}

	const sectionEntryName = getSectionEntryName()
	if (sectionEntryName === "" || sectionEntryName === null) {
		showToast({
			title: i18n.t("manager.actions.addsection.titles.alert", 'Create Entry'),
			message: i18n.t("manager.actions.addsection.alerts.noname.message", "No Entry name provided."),
			type: 'error',
			timeout: 3000
		});
		return false
	}

	return true;
}


function isUriInSection(data, sectionEntryKey, targetUri) {
	if (
		!data ||
		!data.result ||
		!data.result.sections ||
		typeof data.result.sections !== 'object'
	) {
		return false;
	}

	const sectionArray = data.result.sections[sectionEntryKey];

	if (!Array.isArray(sectionArray)) {
		return false;
	}

	return sectionArray.some(item => item.uri === targetUri);
}

const createSection = async (parentUri, parentSectionName) => {

	const template = document.getElementById("cms-section-template-selection").value
	if (template === "000") {
		return false
	}
	try {
		await addSection({
			parentUri: parentUri,
			sectionEntryName: getSectionEntryName(),
			section: parentSectionName,
			template: template
		})
		return true
	} catch (e) {
		return false
	}
}
