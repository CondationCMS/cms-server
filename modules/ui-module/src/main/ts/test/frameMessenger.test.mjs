import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import test from 'node:test';

let messageHandler;
globalThis.window = {
	location: {origin: 'https://cms.example'},
	addEventListener(type, handler) {
		if (type === 'message') {
			messageHandler = handler;
		}
	}
};

const source = await readFile(new URL(
	'../../resources/manager/js/modules/frameMessenger.js',
	import.meta.url
));
const moduleUrl = `data:text/javascript;base64,${source.toString('base64')}`;
const frameMessenger = (await import(moduleUrl)).default;

test('uses the current origin when sending messages', () => {
	let sentMessage;
	const targetWindow = {
		postMessage(message, targetOrigin) {
			sentMessage = {message, targetOrigin};
		}
	};

	frameMessenger.send(targetWindow, {type: 'changed', payload: 42});

	assert.equal(sentMessage.targetOrigin, 'https://cms.example');
	assert.deepEqual(sentMessage.message, {
		__frameMessenger: true,
		type: 'changed',
		payload: 42
	});
	assert.throws(
			() => frameMessenger.send(targetWindow, {type: 'changed'}, '*'),
			/explicit target origin/);
});

test('ignores messages received from another origin', () => {
	let received = 0;
	frameMessenger.on('changed', () => received++);

	messageHandler({
		origin: 'https://attacker.example',
		data: {__frameMessenger: true, type: 'changed'}
	});
	assert.equal(received, 0);

	messageHandler({
		origin: 'https://cms.example',
		data: {__frameMessenger: true, type: 'changed'}
	});
	assert.equal(received, 1);
});
