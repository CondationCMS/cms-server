export namespace UIStateManager {
    function setTabState(key: any, value: any): void;
    function getTabState(key: any, defaultValue?: null): any;
    function setLocale(locale: any): void;
    function getLocale(): any;
    function removeTabState(key: any): void;
    function setAuthToken(token: any): void;
    function getAuthToken(): string | null;
    function clearAuthToken(): void;
}
