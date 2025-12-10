interface Options {
    method: string;
    parameters?: any;
}
declare const executeRemoteCall: (options: Options) => Promise<any>;
declare const executeRemoteMethodCall: (method: string, parameters: any) => Promise<any>;
export { executeRemoteCall, executeRemoteMethodCall };
