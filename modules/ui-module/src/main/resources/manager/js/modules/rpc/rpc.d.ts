interface Options {
    method: string;
    parameters?: any;
}
export interface RPCResponse<T> {
    result: T;
}
export declare class RPCClientError extends Error {
    code: number;
    constructor(code: number, message: string);
}
declare const executeRemoteCall: (options: Options) => Promise<any>;
declare const executeRemoteMethodCall: (method: string, parameters: any) => Promise<any>;
export { executeRemoteCall, executeRemoteMethodCall };
