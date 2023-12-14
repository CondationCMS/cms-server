import { UTF_8 } from 'system/charsets.mjs';
import { $http } from 'system/http.mjs';
import { $template } from 'system/template.mjs';
import { $tags } from 'system/tags.mjs';
import { getLogger } from 'system/logging.mjs';

const logger = getLogger("extensions");
logger.info("debug log from test extension");

// callable via /extensions/test
$http.get("/test", (request, response) => {
	response.addHeader("Content-Type", "text/html; charset=utf-8")
	response.write("ich bin einen test extension!öäü", UTF_8)
})
$http.post("/form", (request, response) => {
	const body = JSON.parse(request.getBody(UTF_8))
	console.log("body", request.getBody(UTF_8))
	console.log(body.form)
	response.addHeader("Content-Type", "text/html; charset=utf-8")
	response.write("ich bin einen test extension!öäü", UTF_8)
})


$template.registerTemplateSupplier(
	"myName",
	() => "Thorsten"
)

$template.registerTemplateFunction(
	"getHello",
	(name) => "Hello " + name + "!"
)

$tags.addTag(
	"hello",
	(params) => `Hello ${params.get("name")}, I'm a TAG!`
)