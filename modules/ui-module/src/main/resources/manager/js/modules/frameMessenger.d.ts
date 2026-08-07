declare namespace _default {
    export { send };
    export { on };
    export { off };
}
export default _default;
declare function send(targetWindow: any, message: any, targetOrigin?: string): void;
declare function on(type: any, callback: any): void;
declare function off(type: any, callback: any): void;
