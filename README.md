# cms

cms is a simple java based flat file content management system.
see wiki for more information: [wiki](https://github.com/thmarx/cms/wiki)


# changelog

## 2.17.0

* **FEATURE** Pages can redirect to external web sites
* **FEATURE** unpublish_date meta field
* **FEATURE** Detected and reload site properties
* **FEATURE** Support for different content types to generate *json* or *html*, default content type is *text/html*
* **FEATURE** New site property *content.type* to set default content type
* **FEATuRE** New meta field *excerpt* to add custom excerpt

## 2.16.0
* **FEATURE** Enable gfm and anchors in markedjs module [#113](https://github.com/thmarx/cms/issues/113)
* **BUGIX** Multiple bugs with visibility of pages [#114](https://github.com/thmarx/cms/issues/114)
* **BUGIX** Remove debug logging from markedjs module [#111](https://github.com/thmarx/cms/issues/111)
* **BUGIX** Error accessing theme modules [#112](https://github.com/thmarx/cms/issues/112)

## 2.15.0
* **FEATURE** SEO module to auto generate xml sitemap [#98](https://github.com/thmarx/cms/issues/98)

## 2.14.0
* **FEATURE** Update markedjs module to version 11.0.0 [#105](https://github.com/thmarx/cms/issues/105)
* **FEATURE** Allow to config multiple hostnames per site [#103](https://github.com/thmarx/cms/issues/103)
* **FEATURE** Preview mode added [#102](https://github.com/thmarx/cms/issues/102)

## 2.13.2

* **BUGFIX** JettyDefaultHandler does not call _callback.succeeded()_

## 2.13.1

* **BUGFIX** Wrong method overloading in query causes pebble engine exception

## 2.13.0

* **FEATURE** GIT integration to add a deployment workflow [#100](https://github.com/thmarx/cms/issues/100)
* **FEATURE** Deliver static files like **robots.txt** [#97](https://github.com/thmarx/cms/issues/97)
* **FEATURE** Make excerpt parameterized [#96](https://github.com/thmarx/cms/issues/96)
* **FEATURE** Exclude content from search via meta field [#95](https://github.com/thmarx/cms/issues/95)
* **BUGFIX** Fix issue while loading media formats [#94](https://github.com/thmarx/cms/issues/94)

## 2.12.0

* **FEATURE** Temp directory per site [#89](https://github.com/thmarx/cms/issues/89)
* **FEATURE** Cropping of images [#88](https://github.com/thmarx/cms/issues/88)
* **FEATURE** Extend query function to get count [#86](https://github.com/thmarx/cms/issues/86)

## 2.11.0

* let query return excerpt from content

## 2.10.0

* Switch to GPLv3 [#82](https://github.com/thmarx/cms/issues/82)
* **BUGIX** Fix issue with query function when using pebble template engine [#81](https://github.com/thmarx/cms/issues/81)
* **FEATURE** Extend query function to allow le, leq, gt and gte [#79](https://github.com/thmarx/cms/issues/79)

## 2.9.1

* Bugfix for nodelist function if current not is not on root level and use relative start path

## 2.9.0

* error, if theme template engine does not match site template engine [#76](https://github.com/thmarx/cms/issues/76)
* add query function to template to select nodes, secondary index is experimental [#37](https://github.com/thmarx/cms/issues/37)

## 2.8.0

* update markedjs markdown renderer to version 10 [#74](https://github.com/thmarx/cms/issues/74)

## 2.7.0

* theme support [#12](https://github.com/thmarx/cms/issues/12)
* legacy server implementation removed [#68](https://github.com/thmarx/cms/issues/68)
