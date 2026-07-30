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
const getVariants = async (options) => {
    const data = {
        method: 'variants.get',
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
const createVariant = async (options) => {
    const data = {
        method: 'variants.create',
        parameters: options
    };
    return (await executeRemoteCall(data)).result;
};
const deleteVariant = async (uri, id) => {
    const data = {
        method: 'variants.delete',
        parameters: { uri, id }
    };
    return (await executeRemoteCall(data)).result;
};
const getVariantSelectors = async (uri) => {
    const data = {
        method: 'variants.selectors.get',
        parameters: { uri }
    };
    return (await executeRemoteCall(data)).result;
};
const setVariantSelector = async (uri, selector) => {
    const data = {
        method: 'variants.selector.set',
        parameters: { uri, selector }
    };
    return (await executeRemoteCall(data)).result;
};
export { createVariant, deleteVariant, getVariants, getVariantSelectors, setVariantSelector };
