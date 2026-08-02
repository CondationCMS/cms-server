/*-
 * #%L
 * UI Module
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import { executeHookAction, executeScriptAction } from '@cms/js/manager-globals.js';
const executeAppAction = (definition) => {
    try {
        const action = JSON.parse(definition);
        if (action.type === 'hook') {
            executeHookAction(action);
        }
        else if (action.type === 'script') {
            executeScriptAction(action);
        }
    }
    catch (error) {
        console.error('Could not execute app action', error);
    }
};
export const initApps = () => {
    document.querySelectorAll('[data-cms-app-action]').forEach(app => {
        app.addEventListener('click', event => {
            event.preventDefault();
            const definition = app.getAttribute('data-cms-app-action');
            if (!definition) {
                return;
            }
            const modalElement = app.closest('.modal');
            const modal = modalElement ? bootstrap.Modal.getInstance(modalElement) : null;
            if (modalElement && modal) {
                modalElement.addEventListener('hidden.bs.modal', () => executeAppAction(definition), { once: true });
                modal.hide();
            }
            else {
                executeAppAction(definition);
            }
        });
    });
};
