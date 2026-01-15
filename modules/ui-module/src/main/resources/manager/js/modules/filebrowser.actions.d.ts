/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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
export function renameFileAction({ state, getTargetFolder, filename }: {
    state: any;
    getTargetFolder: any;
    filename: any;
}): Promise<void>;
export function deleteElementAction({ elementName, state, deleteFN, getTargetFolder }: {
    elementName: any;
    state: any;
    deleteFN: any;
    getTargetFolder: any;
}): Promise<void>;
export function createFolderAction({ state, getTargetFolder }: {
    state: any;
    getTargetFolder: any;
}): Promise<void>;
export function createFileAction({ state, getTargetFolder }: {
    state: any;
    getTargetFolder: any;
}): Promise<void>;
export function createPageActionOfContentType({ state, getTargetFolder, contentType }: {
    state: any;
    getTargetFolder: any;
    contentType: any;
}): Promise<void>;
