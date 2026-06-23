import { $hooks } from 'system/hooks.mjs';
import { UTF_8 } from 'system/charsets.mjs';


$hooks.registerAction("system/server/http/route", ({httpRoutes}) => {
	httpRoutes.add(
			"GET",
			"/ext-route",
			(request, response) => {
				response.write("Hello from an extension", UTF_8)
				return true
			}
	)
	return null
})