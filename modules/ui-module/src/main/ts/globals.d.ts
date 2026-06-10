export {};

declare global {
  interface Window {
    ui: {
      login_url: string;
      manager_url: string;
    };
    manager : {
			csrfToken: string,
			baseUrl: string,
			contextPath: string,
      siteId: string,
      previewUrl: string,
      refreshUrl: string,
      commandPalette: any,
		},
    EasyMDE : any,
    Cherry: any
  }
}

declare var require: any;