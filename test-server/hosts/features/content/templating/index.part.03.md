---
title: Content queries
template: content.part.html
---

Query nodes.

```html
<!-- example for a blog overview page  -->
<div
	th:with='entries = ${query.create("/blog/*").where("featured", true).page(1, 10)}'>
	<th:block th:each="entry : ${entries.items}">
		<h2 th:text="${entry.name}"></h2>
		<p th:text="${entry.content}"></p>
		<u th:text="${#dates.format(entry.meta['publish_date'], 'dd-MM-yyyy HH:mm')}"></u>
		<a th:href="${entry.path}">goto</a>
	</th:block>
</div>
```

