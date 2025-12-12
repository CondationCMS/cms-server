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
import { executeRemoteCall } from '@cms/modules/rpc/rpc.js';
const getSectionTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.sections",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getPageTemplates = async (options) => {
    var data = {
        method: "manager.contentTypes.pages",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getListItemTypes = async (options) => {
    var data = {
        method: "manager.contentTypes.listItemTypes",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const getMediaForm = async (options) => {
    var data = {
        method: "manager.media.form",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const createCSRFToken = async (options) => {
    var data = {
        method: "manager.token.createCSRF",
        parameters: options
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
const getTagNames = async (options) => {
    var data = {
        method: "manager.content.tags",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { getSectionTemplates, getPageTemplates, getMediaForm, getTagNames, getMediaFormats, getListItemTypes, createCSRFToken };
