export function executeScriptAction(action: any): Promise<void>;
export function executeHookAction(action: any): Promise<void>;
/**
 * Patches a relative path so that it's correctly prefixed with the given manager base path.
 *
 * @param {string} relativePath e.g. "/manager/module"
 * @param {string} managerBasePath e.g. "/manager" or "/de/manager"
 * @returns {string} e.g. "/de/manager/module"
 */
export function patchManagerPath(relativePath: string, managerBasePath: string): string;
/**
 * Patches a path with the context path, if not already present.
 *
 * @param {string} path - The original path (e.g. "/assets/images/test.jpg").
 * @returns {string} - The patched path with context prefix if needed.
 */
export function patchPathWithContext(path: string): string;
