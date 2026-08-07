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

export interface FrameMessage<T = unknown> {
	type: string;
	payload?: T;
}

interface FrameMessageEnvelope<T = unknown> extends FrameMessage<T> {
	__frameMessenger: true;
}

export type FrameMessageCallback<T = unknown> = (
	payload: T | undefined,
	event: MessageEvent<unknown>
) => void;

const listeners = new Map<string, FrameMessageCallback<any>[]>();

function send<T>(
	targetWindow: Window,
	message: FrameMessage<T>,
	targetOrigin: string = window.location.origin
): void {
	if (targetOrigin === '*') {
		throw new Error('frameMessenger requires an explicit target origin');
	}
	targetWindow.postMessage({__frameMessenger: true, ...message}, targetOrigin);
}

function on<T = unknown>(type: string, callback: FrameMessageCallback<T>): void {
	if (!listeners.has(type)) {
		listeners.set(type, []);
	}
	listeners.get(type)?.push(callback);
}

function off<T = unknown>(type: string, callback: FrameMessageCallback<T>): void {
	const callbacks = listeners.get(type);
	if (!callbacks) {
		return;
	}
	const index = callbacks.indexOf(callback);
	if (index >= 0) {
		callbacks.splice(index, 1);
	}
}

function isFrameMessage(data: unknown): data is FrameMessageEnvelope {
	if (typeof data !== 'object' || data === null) {
		return false;
	}

	const candidate = data as Record<string, unknown>;
	return candidate.__frameMessenger === true
		&& typeof candidate.type === 'string'
		&& candidate.type.length > 0;
}

function handleMessage(event: MessageEvent<unknown>): void {
	if (event.origin !== window.location.origin || !isFrameMessage(event.data)) {
		return;
	}

	const data = event.data;
	const callbacks = listeners.get(data.type) ?? [];
	for (const callback of callbacks) {
		try {
			callback(data.payload, event);
		} catch (error) {
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
