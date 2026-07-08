declare class State {
    constructor(initialState?: {});
    state: {};
    observers: any[];
    observe(observer: any): void;
    unobserve(observer: any): void;
}
