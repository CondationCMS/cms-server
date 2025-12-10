export namespace EventBus {
    function on(event: any, handler: any): void;
    function emit(event: any, payload: any): void;
}
