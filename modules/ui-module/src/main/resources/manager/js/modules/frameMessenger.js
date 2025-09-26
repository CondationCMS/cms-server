/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
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
// frameMessenger.js
const listeners = new Map();
function send(targetWindow, message, targetOrigin = '*') {
    targetWindow.postMessage({ __frameMessenger: true, ...message }, targetOrigin);
}
function on(type, callback) {
    if (!listeners.has(type)) {
        listeners.set(type, []);
    }
    listeners.get(type).push(callback);
}
function off(type, callback) {
    const callbacks = listeners.get(type);
    if (!callbacks)
        return;
    const index = callbacks.indexOf(callback);
    if (index >= 0) {
        callbacks.splice(index, 1);
    }
}
function handleMessage(event) {
    const data = event.data;
    if (!data || !data.__frameMessenger || !data.type)
        return;
    const callbacks = listeners.get(data.type) || [];
    for (const cb of callbacks) {
        try {
            cb(data.payload, event);
        }
        catch (err) {
            console.error('frameMessenger callback error:', err);
        }
    }
}
window.addEventListener('message', handleMessage);
export default {
    send,
    on,
    off
};
