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
import { RPCResponse } from '@cms/modules/rpc/rpc.js';
declare const getContentNode: (options: any) => Promise<any>;
declare const getContent: (options: any) => Promise<any>;
declare const setContent: (options: any) => Promise<any>;
export interface ReplaceContent {
    uri: string;
}
export interface ReplaceContentOptions {
    uri: string;
    content: string;
    start: number;
    end: number;
}
declare const replaceContent: (options: ReplaceContentOptions) => Promise<RPCResponse<ReplaceContent>>;
declare const setMeta: (options: any) => Promise<any>;
declare const setMetaBatch: (options: any) => Promise<any>;
declare const addSection: (options: any) => Promise<any>;
declare const deleteSection: (options: any) => Promise<any>;
export { getContentNode, getContent, setContent, replaceContent, setMeta, setMetaBatch, addSection, deleteSection };
