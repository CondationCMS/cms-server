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
