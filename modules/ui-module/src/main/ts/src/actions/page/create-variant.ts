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

import { i18n } from '@cms/modules/localization.js'
import { openModal } from '@cms/modules/modal.js'
import { getPreviewUrl, loadPreview } from '@cms/modules/preview.utils.js'
import { getContentNode } from '@cms/modules/rpc/rpc-content.js'
import { getPageTemplates } from '@cms/modules/rpc/rpc-manager.js'
import { createVariant, getVariants } from '@cms/modules/rpc/rpc-variant.js'
import { showToast } from '@cms/modules/toast.js'

const value = (id: string): string =>
	(document.getElementById(id) as HTMLInputElement | HTMLSelectElement)?.value.trim() ?? '';

const validate = (): boolean => {
	const copyContent = (document.querySelector(
		'input[name="cms-variant-content"]:checked'
	) as HTMLInputElement)?.value === 'copy';
	const missing = [
		['cms-variant-id', 'Variant ID'],
		['cms-variant-title', 'Title'],
		...(copyContent ? [] : [['cms-variant-template', 'Template']])
	].find(([id]) => !value(id) || (id === 'cms-variant-template' && value(id) === '__none__'));

	if (!missing) {
		return true;
	}

	showToast({
		title: i18n.t('manager.actions.page.variant.create.validation.title', 'Create variant'),
		message: i18n.t(
			`manager.actions.page.variant.create.validation.${missing[0]}`,
			`${missing[1]} is required.`
		),
		type: 'error',
		timeout: 3000
	});
	return false;
};

export const runAction = async () => {
	try {
		const [activeContentNode, templatesResponse] = await Promise.all([
			getContentNode({ url: getPreviewUrl() }),
			getPageTemplates({})
		]);
		const variantContext = await getVariants({ uri: activeContentNode.result.uri });
		const templates = Array.from(templatesResponse.result ?? [])
			.sort((left: any, right: any) => String(left.name).localeCompare(String(right.name)));
		const currentTemplate = variantContext.canonical.template;

		const options = templates.map((template: any) => {
			const selected = template.template === currentTemplate ? ' selected' : '';
			return `<option value="${escapeHtml(template.template)}"${selected}>${escapeHtml(template.name)}</option>`;
		}).join('');

		openModal({
			title: i18n.t('manager.actions.page.variant.create.title', 'Create page variant'),
			body: `
				<div class="mb-3">
					<label for="cms-variant-id" class="form-label">Variant ID</label>
					<input id="cms-variant-id" class="form-control" required>
				</div>
				<div class="mb-3">
					<label for="cms-variant-title" class="form-label">Title</label>
					<input id="cms-variant-title" class="form-control" required>
				</div>
				<div class="mb-3">
					<label for="cms-variant-template" class="form-label">Template</label>
					<select id="cms-variant-template" class="form-select" required>
						<option value="__none__">Select template</option>
						${options}
					</select>
				</div>
				<fieldset>
					<legend class="fs-6">Initial content</legend>
					<div class="form-check">
						<input class="form-check-input" type="radio" name="cms-variant-content" id="cms-variant-empty" value="empty" checked>
						<label class="form-check-label" for="cms-variant-empty">Empty</label>
					</div>
					<div class="form-check">
						<input class="form-check-input" type="radio" name="cms-variant-content" id="cms-variant-copy" value="copy">
						<label class="form-check-label" for="cms-variant-copy">Copy content and sections from original page</label>
					</div>
				</fieldset>`,
			fullscreen: false,
			validate,
			onCancel: () => {},
			onShow: (modalElement: HTMLElement) => {
				const templateSelect = modalElement.querySelector(
					'#cms-variant-template'
				) as HTMLSelectElement;
				const contentOptions = modalElement.querySelectorAll(
					'input[name="cms-variant-content"]'
				);
				const syncTemplateState = () => {
					const copyContent = (modalElement.querySelector(
						'input[name="cms-variant-content"]:checked'
					) as HTMLInputElement)?.value === 'copy';
					if (copyContent) {
						templateSelect.value = currentTemplate;
					}
					templateSelect.disabled = copyContent;
				};

				contentOptions.forEach(option =>
					option.addEventListener('change', syncTemplateState));
				syncTemplateState();
			},
			onOk: async () => {
				try {
					const copyContent = (document.querySelector(
						'input[name="cms-variant-content"]:checked'
					) as HTMLInputElement)?.value === 'copy';
					const result = await createVariant({
						uri: variantContext.canonical.uri,
						id: value('cms-variant-id'),
						title: value('cms-variant-title'),
						template: copyContent ? currentTemplate : value('cms-variant-template'),
						copyContent
					});
					showToast({
						title: i18n.t('manager.actions.page.variant.create.success.title', 'Variant created'),
						message: i18n.t('manager.actions.page.variant.create.success.message', 'The page variant was created.'),
						type: 'success',
						timeout: 3000
					});
					loadPreview(result.url);
				} catch (error) {
					showError(error);
				}
			}
		});
	} catch (error) {
		showError(error);
	}
};

const escapeHtml = (input: unknown): string => {
	const element = document.createElement('div');
	element.textContent = String(input ?? '');
	return element.innerHTML;
};

const showError = (error: unknown) => showToast({
	title: i18n.t('manager.actions.page.variant.create.error.title', 'Could not create variant'),
	message: error instanceof Error ? error.message : String(error),
	type: 'error',
	timeout: 3000
});
