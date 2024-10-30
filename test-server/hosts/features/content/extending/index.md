---
title: Extending
template: start.html
menu: 
    position: 90
---

## Extensions
Extension are writen in good old JavaScript.

Put your extension into the _host_**/extensions** folder. 
All files with the nameing convention _name_**.extension.js** are loaded ad system startup.

If you need more, you can use [modules](/modules)

### Add custom http endpoint

Attention: Keep in mind, that all http endpoint extensions are loaded unter the endpoint _/extensions_.
What that means is, you can not put content under this url name.

```javascript
import { UTF_8 } from 'system/charsets.mjs';
import { $http } from 'system/http.mjs';

$http.get("/test", (request, response) => {
	response.addHeader("Content-Type", "text/html; charset=utf-8")
	response.write("ich bin einen test extension!öäü", UTF_8)
})
```

The endpoint is available at http://your_host:your_port/extensions/test

### Modules

To structure your extension code, you can create modules. 
But keep in mind, your modules must use the .mjs extension. 
Otherwise our js engine will not load your modules correctly.

### System modules

cms-server comes with some system modules.
All system modules are in the _systems_ package, so you can not use _system_ as folder name for custom modules.

#### http

```javascript
import { header } from 'system/http.mjs';

exchange.getResponseHeaders().add(header("Content-Type"), "text/html; charset=utf-8");
```

#### charsets

```javascript
import { UTF_8, UTF_16, ISO_88591 } from 'system/charsets.mjs';

exchange.getResponseSender().send("I'm a test extension!", UTF_8);
```

#### logging
```javascript
import { getLogger } from 'system/logging.mjs';
const logger = getLogger("extensions");
logger.debug("debug log from test extension");
```

#### template
```javascript
import { $template } from 'system/template.mjs';

$template.registerTemplateSupplier(
	"myName",
	() => "Thorsten"
)

$template.registerTemplateFunction(
	"getHello",
	(name) => "Hello " + name + "!"
)
```
Use template extensions in template
```html
<div th:with="name = ${myName.get()}">
	<p th:th:text="${name}"></p>
	<!-- Thorsten -->
</div>
<div th:with="hello = ${getHello.apply('Thorsten')}">
	<p th:text="${hello}"></p>
	<!-- Hello Thorsten -->
</div>
```

#### files
```javascript
import { $files } from 'system/files.mjs';

let content = $files.readContent("extras/products.json")
```