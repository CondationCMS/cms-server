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

import { i18n } from "../localization.js";

interface DividerOptions {
	name: string;
	title?: string;
}

const createDivider = (options : DividerOptions) => {
	const key = "field." + options.name;
	const title = i18n.t(key, options.title || "");
	const showTitle = title && title.trim().length > 0;

	return `
		<div class="my-4 cms-form-field" data-cms-form-field-type="divider">
			<hr class="mb-1">
			${showTitle ? `<div class="text-muted small text-uppercase fw-bold mb-2" cms-i18n-key="${key}">${title}</div>` : ""}
		</div>
	`;
};

const getData = () => {
	return {}; // Divider liefert keine Daten zurück
};

export const Divider = {
	markup: createDivider,
	init: () => {},
	data: getData
};
