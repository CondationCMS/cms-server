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

export interface GetTransitionsRequest {
	uri: string; // The URI of the folder where the page should be created
}
export interface GetTransitionsDto {
	id: string,
	label: string,
	error?: boolean,
	message?: string,
	code?: number
}
const getWfTransitions = async (options: GetTransitionsRequest) => {
	var data = {
		method: "workflow.transitions.get",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as { transitions: GetTransitionsDto[]};
};

export interface GetStatusRequest {
	uri: string; // The URI of the folder where the page should be created
}
export interface GetStatusDto {
	published: boolean,
	withinSchedule: boolean,
	error?: boolean,
	message?: string,
	code?: number
}
const getWfStatus = async (options: GetStatusRequest) => {
	var data = {
		method: "workflow.node.status",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as GetStatusDto;
};

export interface WfTransitRequest {
	uri: string; // The URI of the folder where the page should be created
	transitionId: string; // The URI of the folder where the page should be created
}
export interface WFTransitDto {
	success?: boolean,
	error?: boolean
}
const wfTransit = async (options: WfTransitRequest) => {
	var data = {
		method: "workflow.node.status",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as WFTransitDto;
};

export { getWfTransitions, getWfStatus, wfTransit };
