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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import { i18n } from '@cms/modules/localization.js';
import { ActivePreviewContent } from '@cms/modules/preview-context.js';
import { showToast } from '@cms/modules/toast.js';

export const variantsSupported = (content?: ActivePreviewContent | null): boolean =>
	Boolean(content?.uri)
		&& content?.supportsVariants !== false
		&& content?.contentKind !== 'collection';

export const ensureVariantsSupported = (content?: ActivePreviewContent | null): boolean => {
	if (variantsSupported(content)) {
		return true;
	}
	showToast({
		title: i18n.t('manager.actions.page.variants.unsupported.title', 'Variants not supported'),
		message: i18n.t(
			'manager.actions.page.variants.unsupported.message',
			'Collections do not support variants.'
		),
		type: 'info',
		timeout: 3000
	});
	return false;
};
