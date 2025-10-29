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

import { executeRemoteCall } from './rpc.js'

export interface GetTranslationsOptions {
	uri: string; // The URI of the folder where the page should be created
}
export interface TranslationDto {
	site: string,
	lang: string,
	country: string,
	url?: string,
	managerDeepLink?: string
}
const getTranslations = async (options: GetTranslationsOptions) => {
	var data = {
		method: "translations.get",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as { translations: TranslationDto[]};
};

export interface AddTranslationOptions {
	uri: string
	language: string
	translationUri: string
}
export interface AddTranslationResult {
	uri: string
	error? : boolean
}
const addTranslation = async (options: AddTranslationOptions) => {
	var data = {
		method: "translations.add",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as AddTranslationResult;
};

export interface RemoveTranslationOptions {
	uri: string
	lang: string
}
export interface RemoveTranslationResult {
	uri: string
	error? : boolean
}
const removeTranslation = async (options: RemoveTranslationOptions) => {
	var data = {
		method: "translations.add",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as RemoveTranslationResult;
};

export { getTranslations, addTranslation, removeTranslation };
