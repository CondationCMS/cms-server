import { RPCResponse } from '@cms/modules/rpc/rpc.js';
declare const getContentNode: (options: any) => Promise<any>;
declare const getContent: (options: any) => Promise<any>;
declare const setContent: (options: any) => Promise<any>;
export interface ReplaceContent {
    error: boolean | null;
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
