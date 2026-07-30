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

export interface ActivePreviewContent {
	uri: string;
	url?: string;
	canonicalUri?: string;
	variantId?: string | null;
}

let activeContent: ActivePreviewContent | null = null;

const setActivePreviewContent = (content: ActivePreviewContent | null) => {
	activeContent = content?.uri ? { ...content } : null;
	window.dispatchEvent(new CustomEvent(
		"cms:preview-context-changed",
		{ detail: activeContent }
	));
};

const comparableUrl = (url: string): string | null => {
	try {
		const parsed = new URL(url, window.location.origin);
		parsed.hash = '';
		return `${parsed.pathname}${parsed.search}`;
	} catch {
		return null;
	}
};

const getActivePreviewContent = (
	currentPreviewUrl?: string
): ActivePreviewContent | null => {
	if (!activeContent) {
		return null;
	}
	if (!currentPreviewUrl || !activeContent.url) {
		return activeContent;
	}
	const activeUrl = comparableUrl(activeContent.url);
	const currentUrl = comparableUrl(currentPreviewUrl);
	return activeUrl !== null && activeUrl === currentUrl
		? activeContent
		: null;
};

export { getActivePreviewContent, setActivePreviewContent };
