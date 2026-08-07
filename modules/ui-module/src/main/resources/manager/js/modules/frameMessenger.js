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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
const listeners = new Map();
function send(targetWindow, message, targetOrigin = window.location.origin) {
    if (targetOrigin === '*') {
        throw new Error('frameMessenger requires an explicit target origin');
    }
    targetWindow.postMessage({ __frameMessenger: true, ...message }, targetOrigin);
}
function on(type, callback) {
    if (!listeners.has(type)) {
        listeners.set(type, []);
    }
    listeners.get(type)?.push(callback);
}
function off(type, callback) {
    const callbacks = listeners.get(type);
    if (!callbacks) {
        return;
    }
    const index = callbacks.indexOf(callback);
    if (index >= 0) {
        callbacks.splice(index, 1);
    }
}
function isFrameMessage(data) {
    if (typeof data !== 'object' || data === null) {
        return false;
    }
    const candidate = data;
    return candidate.__frameMessenger === true
        && typeof candidate.type === 'string'
        && candidate.type.length > 0;
}
function handleMessage(event) {
    if (event.origin !== window.location.origin || !isFrameMessage(event.data)) {
        return;
    }
    const data = event.data;
    const callbacks = listeners.get(data.type) ?? [];
    for (const callback of callbacks) {
        try {
            callback(data.payload, event);
        }
        catch (error) {
            console.error('frameMessenger callback error:', error);
        }
    }
}
window.addEventListener('message', handleMessage);
export default {
    send,
    on,
    off
};
