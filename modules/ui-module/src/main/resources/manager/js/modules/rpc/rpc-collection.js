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
export const listCollectionItems = async (options) => {
    return (await executeRemoteCall({
        method: 'collections.items',
        parameters: options
    })).result;
};
export const listCollectionItemsCursor = async (options) => {
    return (await executeRemoteCall({
        method: 'collections.items.cursor',
        parameters: options
    })).result;
};
export const getCollectionItem = async (collection, id) => {
    return (await executeRemoteCall({
        method: 'collections.item.get',
        parameters: { collection, id }
    })).result;
};
export const saveCollectionItem = async (options) => {
    await executeRemoteCall({
        method: 'collections.item.save',
        parameters: options
    });
};
export const createCollectionItem = async (options) => {
    return (await executeRemoteCall({
        method: 'collections.item.create',
        parameters: options
    })).result;
};
export const deleteCollectionItem = async (collection, id) => {
    await executeRemoteCall({
        method: 'collections.item.delete',
        parameters: { collection, id }
    });
};
