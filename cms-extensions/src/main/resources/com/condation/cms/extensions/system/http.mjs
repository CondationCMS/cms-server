const METHODS = {
	GET : "GET",
	POST : "POST",
	PUT : "PUT",
	UPDATE: "UPDATE"
}
export const $http = {
	get : (path, handler) => {
		extensions.registerHttpExtension(METHODS.GET, path, handler)
	},
	post : (path, handler) => {
		extensions.registerHttpExtension(METHODS.POST, path, handler)
	}
}