import { UTF_8 } from 'system/charsets.mjs';
import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("system/server/http/extension", (context) => {
	context.arguments().get("httpExtensions").add(
			"GET",
			"/test2",
			(request, response) => {
				response.addHeader("Content-Type", "text/html; charset=utf-8")
				response.write("http extension via hook!", UTF_8)
			}
	)
	return null;
})

$hooks.registerAction("system/server/http/route", (context) => {
	context.arguments().get("httpRoutes").add(
			"GET",
			"/hello-route",
			(request, response) => {
				response.addHeader("Content-Type", "text/html; charset=utf-8")
				response.write("route via hook!", UTF_8)
			}
	)
	return null;
})

$hooks.registerAction("system/server/api/route", (context) => {
	context.arguments().get("apiRoutes").add(
			"GET",
			"/hello",
			(request, response) => {

				let data = {
					"name": "CondationCMS"
				}

				response.addHeader("Content-Type", "application/json; charset=utf-8")
				response.write(JSON.stringify(data), UTF_8)
			}
	)
	return null;
})
