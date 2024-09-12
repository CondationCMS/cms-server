export const $query = {
	registerOperation : (name, fun) => {
		extensions.registerQueryOperation(name, fun)
	}
}