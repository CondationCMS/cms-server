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
const getSectionEntryTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.sectionEntries",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getPageTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.pages",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getListItemTypes = async (options) => {
    var data = {
        method: "manager.contentTypes.listItemTypes",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const getMediaForm = async (options) => {
    var data = {
        method: "manager.media.form",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
const createCSRFToken = async (options) => {
    var data = {
        method: "manager.token.createCSRF",
        parameters: options || {}
    };
    return await executeRemoteCall(data);
};
export var Format;
(function (Format) {
    Format[Format["WEBP"] = 0] = "WEBP";
    Format[Format["JPEG"] = 1] = "JPEG";
    Format[Format["PNG"] = 2] = "PNG";
})(Format || (Format = {}));
const getMediaFormats = async (options) => {
    var data = {
        method: "manager.media.formats",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getShortCodeNames = async (options) => {
    var data = {
        method: "manager.content.shortCodes",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { getSectionEntryTemplates, getPageTemplates, getMediaForm, getShortCodeNames, getMediaFormats, getListItemTypes, createCSRFToken };
