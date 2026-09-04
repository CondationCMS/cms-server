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

import { executeRemoteCall } from '@cms/modules/rpc/rpc.js'

export interface FormDefinition {
	fields: FormFieldDefinition[];
}

export interface FormFieldDefinition {
	type: string;
	name: string;
	title: string;
	required: boolean;
	requiredMessage?: string;
}

export interface PageTemplate {
	name: string;
	template: string;
	forms: Record<string, FormDefinition>;
	contentFolder: string;
	createButton: boolean;
}

export interface SectionEntryTemplate {
	section: string;
	name: string;
	template: string;
	forms: Record<string, FormDefinition>;
}

export interface ListItemType {
	name: string;
	form: FormDefinition;
}

export interface CollectionType {
	name: string;
	label: string;
	forms: Record<string, FormDefinition>;
}

interface ContentTypeResponse<T> {
	result: T[];
}

const getSectionEntryTemplates = async (options : any): Promise<ContentTypeResponse<SectionEntryTemplate>> => {
	var data = {
		method: "manager.contentTypes.sectionEntries",
		parameters: options || {}
	}
	return await executeRemoteCall(data);
};

const getPageTemplates = async (options : any): Promise<ContentTypeResponse<PageTemplate>> => {
	var data = {
		method: "manager.contentTypes.pages",
		parameters: options || {}
	}
	return await executeRemoteCall(data);
};

const getListItemTypes = async (options : any): Promise<ContentTypeResponse<ListItemType>> => {
	var data = {
		method: "manager.contentTypes.listItemTypes",
		parameters: options || {}
	}
	return await executeRemoteCall(data);
};

const getCollectionTypes = async (): Promise<ContentTypeResponse<CollectionType>> => {
	return await executeRemoteCall({
		method: "manager.contentTypes.collections",
		parameters: {}
	});
};

const getMediaForm = async (options : any) => {
	var data = {
		method: "manager.media.form",
		parameters: options || {}
	}
	return await executeRemoteCall(data);
};

const createCSRFToken = async (options : any) => {
	var data = {
		method: "manager.token.createCSRF",
		parameters: options || {}
	}
	return await executeRemoteCall(data);
};

export enum Format {
  WEBP,
  JPEG,
  PNG
}

export interface MediaFormat {
  name: string;
  height?: number;
  width?: number;
  format: Format;
  compression: boolean;
  cropped: boolean
}

export interface MediaFormatsResponse {
  result: MediaFormat[];
}
const getMediaFormats = async (options : any): Promise<MediaFormatsResponse> => {
	var data = {
		method: "manager.media.formats",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const getShortCodeNames = async (options : any) => {
	var data = {
		method: "manager.content.shortCodes",
		parameters: options
	}
	return await executeRemoteCall(data);
};

export { 
	getSectionEntryTemplates, 
	getPageTemplates, 
	getMediaForm, 
	getShortCodeNames, 
	getMediaFormats,
	getListItemTypes,
	getCollectionTypes,
	createCSRFToken
};
