---
title: List of pages
template: content.part.html
---

Whenever you want to print out a list of pages, the nodeListFunction is a tiny little helper.  
Keep in mind, that the nodelist function does not return the hole rendered HTML content but just an excerpt of 200 characters.

```html
<!-- example for a blog overview page  -->
<div
	th:with='page = ${nodeList.from("/blog/*").sort("publish_date").reverse(true).page(1).size(5).list()}'>
	<th:block th:each="entry : ${page.items}">
		<h2 th:text="${entry.name}"></h2>
		<p th:text="${entry.content}"></p>
		<u th:text="${#dates.format(entry.meta['publish_date'], 'dd-MM-yyyy HH:mm')}"></u>
		<a th:href="${entry.path}">goto</a>
	</th:block>
</div>
```

#### UseCase Blog
Assume, your project has the following content structure for the blog, with the defaults _page = 1_ and _pageSize = 5_
```
blog/
---2023-09/
------entry1.md
------entry2.md
---2023-10/
------entry1.md
```
This nodeList call will return all content nodes in all subfolders of the folder blog/
```javascript
${nodeList.from("/blog/*").list()}
```
