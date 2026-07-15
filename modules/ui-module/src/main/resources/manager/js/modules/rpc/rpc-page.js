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
import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const createPage = async (options) => {
    var data = {
        method: "page.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const filterPages = async (options) => {
    var data = {
        method: "pages.filter",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deletePage = async (options) => {
    var data = {
        method: "page.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const searchPages = async (options) => {
    var data = {
        method: "pages.search",
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
export { createPage, deletePage, filterPages, searchPages };
