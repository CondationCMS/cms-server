/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
