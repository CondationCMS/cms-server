---
title: Navigation
template: content.part.html
---

Navigation is realy simple.

```html
<ul th:with="nodes=${navigation.list('/')}">
    <li th:each="node : ${nodes}" th:if="${node.path} != '/'">
        <a th:attr="aria-current=${node.current ? 'page' : ''}"
            th:classappend="${node.current}? 'active'"
            th:href="${node.path}"
			th:text="${node.name}"></a>
	</li>
</ul>
```