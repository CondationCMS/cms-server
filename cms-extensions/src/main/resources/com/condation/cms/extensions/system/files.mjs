import { UTF_8 } from 'system/charsets.mjs';

export const $files = {
	readContent : (file, charset) => {
		let theFile = fileSystem.resolve(file)
		return fileSystem.loadContent(theFile, typeof charset !== "undefined" ? charset : UTF_8)
	}
}