const METHODS = {
	GET : "GET",
	POST : "POST",
	PUT : "PUT",
	UPDATE: "UPDATE"
}
export const $routes = {
	get : (path, handler) => {
		extensions.registerHttpRouteExtension(METHODS.GET, path, handler)
	},
	post : (path, handler) => {
		extensions.registerHttpRouteExtension(METHODS.POST, path, handler)
	}
}