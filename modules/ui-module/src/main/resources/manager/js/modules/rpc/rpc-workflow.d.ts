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
export interface GetTransitionsRequest {
    uri: string;
}
export interface GetTransitionsDto {
    id: string;
    label: string;
    error?: boolean;
    message?: string;
    code?: number;
}
declare const getWfTransitions: (options: GetTransitionsRequest) => Promise<{
    transitions: GetTransitionsDto[];
}>;
export interface GetStatusRequest {
    uri: string;
}
export interface GetStatusDto {
    published: boolean;
    withinSchedule: boolean;
    error?: boolean;
    message?: string;
    code?: number;
}
declare const getWfStatus: (options: GetStatusRequest) => Promise<GetStatusDto>;
export interface WfTransitRequest {
    uri: string;
    transitionId: string;
}
export interface WFTransitDto {
    success?: boolean;
    error?: boolean;
}
declare const wfTransit: (options: WfTransitRequest) => Promise<WFTransitDto>;
export { getWfTransitions, getWfStatus, wfTransit };
