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
	uri?: string;
}
export interface ItemDto {
	uri: string;
	meta?: any;
}
export interface UnpublishedPagesRequest {
	page?: number;
	size?: number;
}
export interface UnpublishedPagesDto {
	totalItems: number;
	pageSize: number;
	totalPages: number;
	page: number;
	items: ItemDto[];
}
const getUnpublishedPages = async (options: UnpublishedPagesRequest): Promise<UnpublishedPagesDto> => {
	const data = {
		method: "workflow.pages.unpublished",
		parameters: options
	};
	return (await executeRemoteCall(data)).result as UnpublishedPagesDto;
};
export interface GetTransitionsDto {
	id: string,
	label: string
}
const getWfTransitions = async (options: GetTransitionsRequest) => {
	var data = {
		method: "workflow.transitions.get",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as { transitions: GetTransitionsDto[]};
};

export interface GetWFManagerRequest {
	uri?: string;
}
export interface GetWFManagerStatusDto {
	published: boolean,
	withinSchedule: boolean,
	currentStage: string,
	publish_date?: Date,
	unpublish_date?: Date
}
export interface GetWFManagerTransitionsDto {
	id: string,
	label: string,
	description: string,
}
export interface getWFManagerDto {
	status?: GetWFManagerStatusDto
	transitions?: GetWFManagerTransitionsDto[]
}
const getWfManagerStatus = async (options: GetWFManagerRequest) => {
	var data = {
		method: "workflow.manager.node.status",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as getWFManagerDto;
};

export interface WfTransitRequest {
	uri?: string;
	transitionId: string; // The URI of the folder where the page should be created
}
export interface WFTransitDto {
	success?: boolean
}
const wfTransit = async (options: WfTransitRequest) => {
	var data = {
		method: "workflow.transit",
		parameters: options
	}
	return (await executeRemoteCall(data)).result as WFTransitDto;
};

export { getWfTransitions, getWfManagerStatus, wfTransit, getUnpublishedPages };
