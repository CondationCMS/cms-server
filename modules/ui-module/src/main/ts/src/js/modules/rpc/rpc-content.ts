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

const getContentNode = async (options : any) => {
	var data = {
		method: "content.node",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const getContent = async (options : any) => {
	var data = {
		method: "content.get",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const setContent = async (options : any) => {
	var data = {
		method: "content.set",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const setMeta = async (options : any) => {
	var data = {
		method: "meta.set",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const setMetaBatch = async (options : any) => {
	var data = {
		method: "meta.set.batch",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const addSection = async (options : any) => {
	var data = {
		method: "content.section.add",
		parameters: options
	}
	return await executeRemoteCall(data);
};

const deleteSection = async (options : any) => {
	var data = {
		method: "content.section.delete",
		parameters: options
	}
	return await executeRemoteCall(data);
};

export { getContentNode, getContent, setContent, setMeta, setMetaBatch, addSection, deleteSection };
