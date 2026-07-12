---
title: Startpage
template: start.html
search:
  index: false
status: published
description: Thats awesome 1234
parent:
  text: another text for the meta attribute , seems to work
count: 20
background_color: '#c2e0c6'
range_test: 42
choose_color: green
unpublish_date: null
publish_date: null
features:
- export
checked:
- farbe
selected: form
radioed: form
media_url: images/fff.png
object:
  values:
  - title: Test 1 and more
    description: desc 1 - 2
  - title: Test 2 update
  - title: test
    description: blub
    features: search
seo:
  description: hier kommt die beschreibung
linked_page: about
translations:
  de: /
taxonomy:
  tags:
  - kleidung
  - hoodies_sweatshirts
  - schuhe
  - kinderkleidung
  - Small Test
  - New tag
---

# Demo Project

![TestBild!](/media/images/test.jpg?format=small)

That's a demo page with some extra features to show the manager application!

Hello world!

Here some content!

Hello: [[cms:username]][[/cms:username]]  
Theme: [[ext:theme_name]][[/ext:theme_name]]

[about](/about)

[this is a new page](/this-is-a-new-page)


```java
// its a comment
System.out.println("Hello world!");
```

### say hello
[[ext:say_hello name="CondationCMS" /]]


### test ShortCode with content
---
[[ext:bold_content]]This content will be bold[[/ext:bold_content]]
---


### example from module
[[ext:example /]]
