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

const getPageTemplates = () => {
	var info = document.getElementById("contentPreview").contentWindow.cmsUIInfo;
	if (!info) {
		return []
	}

	return info.pageTemplates
}

const getMetaForm = () => {
	var info = document.getElementById("contentPreview").contentWindow.cmsUIInfo;
	if (!info || !info.metaForm) {
		return []
	}

	return info.metaForm
}

const getSectionTemplates = (section) => {
	var info = document.getElementById("contentPreview").contentWindow.cmsUIInfo;
	if (!info || !info.sectionTemplates || !info.sectionTemplates[section]) {
		return {}
	}

	return info.sectionTemplates[section];
}

export { getPageTemplates, getMetaForm, getSectionTemplates };
