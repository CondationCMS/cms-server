export {};

declare global {
  interface Window {
    ui: {
      login_url: string;
      manager_url: string;
    };
    manager : {
			csrfToken: string
			baseUrl: string,
			contextPath: string,
      siteId: string,
		},
    EasyMDE : any,
    Cherry: any
  }
}

declare var require: any;

// manager-globals
declare function executeScriptAction(action: any): Promise<any>;
declare function executeHookAction(action: any): Promise<any>;
declare function patchManagerPath (relativePath: string, managerBasePath: string): string;
declare function patchPathWithContext(path: string): string;