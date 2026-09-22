export interface FrameMessage<T = unknown> {
    type: string;
    payload?: T;
}
export type FrameMessageCallback<T = unknown> = (payload: T | undefined, event: MessageEvent<unknown>) => void;
declare function send<T>(targetWindow: Window, message: FrameMessage<T>, targetOrigin?: string): void;
declare function on<T = unknown>(type: string, callback: FrameMessageCallback<T>): void;
declare function off<T = unknown>(type: string, callback: FrameMessageCallback<T>): void;
declare const _default: {
    send: typeof send;
    on: typeof on;
    off: typeof off;
};
export default _default;
