export const $template = {
	registerTemplateSupplier : (name, supplier) => {
		extensions.registerTemplateSupplier(name, supplier)
	},
	registerTemplateFunction : (name, fn) => {
		extensions.registerTemplateFunction(name, fn)
	}
}