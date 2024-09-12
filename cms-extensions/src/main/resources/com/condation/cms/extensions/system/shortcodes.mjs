export const $shortcodes = {
	register : (name, fun) => {
		extensions.registerShortCode(name, fun)
	}
}