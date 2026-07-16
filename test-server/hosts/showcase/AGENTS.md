# deine rolle

für das erstelle der templates bist du in der rolle eines senio ui ux designers.

für erstellung von content in der rolle eines senior content creator.


# Tech Stack
- **Backend:** Java 25 (Condation Server)
- **Frontend:** Tailwind CSS (Play CDN), Alpine.js, Bootstrap Icons
- **Content:** Markdown mit YAML Frontmatter
- **Templating:** Twig-ähnliche Syntax (mit Liquid-Erweiterungen wie `{% assign %}`)

# Verzeichnisstruktur
- `/content`: Markdown-Dateien für die Seiteninhalte. Ordner-Struktur entspricht URL-Struktur
- `/templates`: HTML-Templates (`base.html` ist das Basis-Layout).
- `/assets`: Statische Dateien (CSS, JS, Icons).
- `/config`: Taxonomie-Definitionen (Marken, Produkte).
- `/messages`: Lokalisierung (Properties-Dateien).
- `/extensions`: in javascript geschriebene Extensions


# taxonomy

aktuell gehen nur tags:

taxonomies.yaml
```yaml
## YAML Template.
---
taxonomies:
  - title: Tags
    slug: tags
    field: taxonomy.tags # feld in den metadaten
    array: true
```

taxonomy.tags.yaml
```yaml
---
values:
- id: schuhe
  title: Schuhe
- id: kinderkleidung
  title: Kinderkleidung
- id: kleidung
  title: Kleidung
- id: hoodies_sweatshirts
  title: Hoodies & Sweatshirts
- id: small-test
  title: Small Test
- id: new-tag
  title: New tag
```

es werden dann automatisch überischtsseite generiert:
/tags für die tag übesicht
/tags/schuhe für den tag schuhe

im template kann mit cms.taxonomies.url("tags", "schuhe"), eine url erstellt werden.


# Templating Syntax (Twig/Liquid-Hybrid)

## Variablen & Ausgabe
- `{{ node.meta }}`: Eigenschaft des aktuellen Knotens ausgeben. Zugriff auf Meta-Attribute (Yaml Header). Bsp. Titel: {{ node.meta.title }} oder nullsafe {{ node.meta.getOrDefault("title", "default titel") }}
- zugriff ist auch mit punkt notation möglich: {{ node.meta.getOrDefault("seo.description", "default description")}}
- `{{ requestContext.getQueryParameter('page') }}`: Zugriff auf Request-Parameter.
- Der genrenderete Inhalt eine Seite liegt in `{{ node.content | raw}}`, der raw filter sorgt dafür, dass es unescaped ausgegeben wird.

## Kontrollstrukturen
```twig
{% if condition %}
  ...
{% else %}
  ...
{% endif %}

{% for item in list %}
  ...
{% endfor %}
```

## Zuweisungen (Liquid Style)
```twig
{% assign variableName = value %}
```

## Layouts mit Blöcken
Base template _base.html_ definiert Blöcke
```twig
{% block name %} {% endblock %}
```

Child template extendet das base tempalte.
```twig
{% extends "base.html" %}
{% block name %} Standardinhalt {% endblock %}
```

## Filter

Filter werden Beispielweise wie folgt genutzt:

```twig
{{ page.totalPages | minus(1) }}
```

Es gibt folgende Filter
- **minus(<ZAHL>)** Zieht die angegebene Zahl ab
- **plus(<ZAHL>)** Addiert die angegebene Zahl
- **date(<format>)** Formatiert ein Datumsobject nach der Java Datumsformat konvetion
- **raw** Sorgt dafür, dass Text/HTML unescaped ausgegeben wird
- **markdown** Der Text läuft durch den markdown renderer

## Template Komponenten

```twig
{[ ext:hello name="CondationCMS" color="red" ]}
{[ /ext:hello ]}
```

Erstellung über ein extensions
```js
import { $hooks } from 'system/hooks.mjs';

$hooks.registerAction("system/template/component", (context) => {
	context.arguments().get("components").put(
			"hello",
			(params) => `<div style="color: ${params.color}">${params.name}</div>`
	)
	return null;
})
```

## Markdown Content Struktur
Jede Inhaltsseite beginnt mit einem YAML-Frontmatter:
```yaml
---
title: Seiten Titel
template: template-name.html
published: true
menu:
    position: 1
    visible: true
---
# Markdown Inhalt hier
```

## ShortCodes

Über Tags können Erweiterungen für Markdown erstellt werden.
```markdown
[[ext:hello name="CondationCMS" /]]
```

```js
import { $hooks } from 'system/hooks.mjs';


$hooks.registerAction("system/content/shortcode", ({shortCodes}) => {
	shortCodes.put(
			"hello",
			(params) => `Hello, ${params.name}`
	)
	return null;
}) 
```

# Entwicklungshinweise
- **CSS:** Tailwind CSS wird aktuell über das CDN eingebunden (`<script src="https://unpkg.com/@tailwindcss/browser@4"></script>`).
- **Interaktivität:** Alpine.js wird für Dropdowns und mobile Menüs genutzt.
- **Icons:** Bootstrap Icons liegen lokal in `/assets/bootstrap-icons-1.11.3/`.
- **Performance:** Da es ein Flat-File System ist, auf effiziente Abfragen von Taxonomien achten.

## Übersichtsseiten:
so werden übersichtsseiten gebaut:

```
{% assign pageNumber = requestContext.getQueryParameter('page', '1') %}
{% assign page = cms.nodeList.from("/news/*").sort("publish_date").reverse(true).page(pageNumber).size(3).list() %}

{% for entry in page.items %}
	{{ entry.meta['title] }}
	{{ {{ cms.links.createUrl(entry.path) }} }}
{% endfor %}

```

Das Page-Object, dass von nodeList geliefert wird, enthält folgende informationen:
- **page.items** Die Ergebnisse für diese Seite
- **page.totalItems** Die gesamt Anzahl der Ergebnisse
- **page.pageSize** Anzahl der Ergebnisse pro Seite
- **page.totalPages** Anzahl der Saiten
- **page.page** die aktuelle Seitennummer

## Medien

Medien liegen im Ordner assets/. 
referenziert werden sie im template /assets/<bildname>


mehr informationen findest du hier: https://condation.com/documentation/cms


# Projekt
Information zum Projekt findest du in PROJECT.md