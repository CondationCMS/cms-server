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

import { executeRemoteCall } from '@cms/modules/rpc/rpc.js'

export interface PageCreateOptions {
	uri: string; // The URI of the folder where the page should be created
	name: string; // The name of the page to be created
	contentType: string; // Optional content type for the page
}
const createPage = async (options: PageCreateOptions) => {
	var data = {
		method: "page.create",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const deletePage = async (options: any) => {
	var data = {
		method: "page.delete",
		parameters: options
	}
	return await executeRemoteCall(data);
};

export { createPage, deletePage };
