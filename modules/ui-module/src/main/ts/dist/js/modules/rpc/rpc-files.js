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
import { executeRemoteCall } from './rpc.js';
const listFiles = async (options) => {
    var data = {
        method: "files.list",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deleteFile = async (options, any) => {
    var data = {
        method: "files.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const deleteFolder = async (options) => {
    var data = {
        method: "files.delete",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const createFolder = async (options) => {
    var data = {
        method: "folders.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const createFile = async (options) => {
    var data = {
        method: "files.create",
        parameters: options
    };
    return await executeRemoteCall(data);
};
const renameFile = async (options) => {
    var data = {
        method: "files.rename",
        parameters: options
    };
    return await executeRemoteCall(data);
};
export { listFiles, deleteFile, createFolder, createFile, deleteFolder, renameFile };
