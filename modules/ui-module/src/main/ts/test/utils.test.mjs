import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import test from 'node:test';

const source = await readFile(new URL(
	'../../resources/manager/js/modules/utils.js',
	import.meta.url
));
const moduleUrl = `data:text/javascript;base64,${source.toString('base64')}`;
const {uuid} = await import(moduleUrl);

test('uses crypto.randomUUID when the browser provides it', () => {
	const expected = '7cf9ab7b-4866-4e8a-aac1-f3f337d35a85';
	globalThis.window = {
		crypto: {
			randomUUID: () => expected,
			getRandomValues: () => {
				throw new Error('fallback must not be used');
			}
		}
	};

	assert.equal(uuid(), expected);
});

test('creates an RFC 4122 UUID v4 with getRandomValues as fallback', () => {
	globalThis.window = {
		crypto: {
			getRandomValues: bytes => {
				bytes.forEach((_, index) => bytes[index] = index);
				return bytes;
			}
		}
	};

	assert.equal(uuid(), '00010203-0405-4607-8809-0a0b0c0d0e0f');
});
