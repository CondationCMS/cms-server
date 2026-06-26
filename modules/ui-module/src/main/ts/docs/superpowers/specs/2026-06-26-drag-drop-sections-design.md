# Drag & Drop Section Sorting — Design Spec

**Date:** 2026-06-26  
**Branch:** e2e_example_module  

## Overview

Sections im CMS-Preview sollen per Drag & Drop direkt umsortierbar sein. Die Konfiguration erfolgt an der section-Container-Toolbar via neuem Action-Typ `"dragSectionEntries"`, parallel zum bestehenden `"orderSectionEntries"` (Modal). Nach dem Drop wird automatisch gespeichert, ohne die Preview-Seite neu zu laden.

## Anforderungen

- Desktop-Browser: Chrome, Firefox, Safari, Edge (aktuell)
- Touch-Support: nicht erforderlich
- Layout-agnostisch: funktioniert für horizontale (Flex-Row, Grid) und vertikale (Flex-Column) Section-Layouts
- Auto-Save on Drop: kein expliziter Speichern-Button, kein Seiten-Reload
- Rückwärtskompatibel: `"orderSectionEntries"` (Modal) bleibt unverändert

## Konfiguration

Der Entwickler wählt in seinem Template einen der beiden Action-Typen — oder beide:

```html
{{/* Nur Drag & Drop */}}
{{ ext.ui.toolbar("asection", "section", ["addSectionEntry", "dragSectionEntries"], { "section": "asection"}) | raw }}

{{/* Nur Modal */}}
{{ ext.ui.toolbar("asection", "section", ["addSectionEntry", "orderSectionEntries"], { "section": "asection"}) | raw }}
```

Die sectionEntry-Toolbar bleibt unverändert:
```html
{{ ext.ui.toolbar(node.uri, "sectionEntry", ["editContent", "editAttributes", "deleteSectionEntry"], {"uri": node.uri, "form": "attributes"}) | raw }}
```

## Architektur & Datenfluss

```
Preview-Frame (toolbar.inject.ts)
  1. initToolbar() erkennt "dragSectionEntries" auf section-Container
  2. requestAnimationFrame(() => initDragDrop(container, sectionName))
       — stellt sicher, dass alle sectionEntry-Toolbars bereits initialisiert sind
  3. Für jedes sectionEntry-Kind-Element:
       - draggable="true" setzen
       - MOVE_ICON-Handle als Button in dessen Toolbar injizieren
  4. Container: dragstart / dragover / drop Events registrieren
  5. onDrop: URIs in neuer Reihenfolge aus dem DOM lesen → updates[] aufbauen
  6. frameMessenger.send(window.parent, {
         type: "sort-sections",
         payload: { updates: [{ uri, meta: { "layout.order": { type: "number", value: index } } }] }
     })

Manager-Frame (manager.message.handlers.ts)
  7. Neuer Handler frameMessenger.on("sort-sections", payload => ...)
  8. setMetaBatch({ updates: payload.updates }) direkt aufrufen
  9. Kein reloadPreview(), kein Modal
```

## Layout-agnostische Drop-Ziel-Erkennung

Beim `dragover` wird für jedes sectionEntry-Kind der Abstand zwischen Mausposition und Element-Mitte berechnet (pythagoreisch). Das Element mit dem kleinsten Abstand ist das Ziel. Der Cursor-Vergleich zur Mitte bestimmt `insertBefore` vs. `insertAfter`:

```
für jedes sectionEntry-Kind k:
  rect = k.getBoundingClientRect()
  dx = event.clientX - (rect.left + rect.width / 2)
  dy = event.clientY - (rect.top + rect.height / 2)
  distance = Math.sqrt(dx*dx + dy*dy)

nächstes Element = Kind mit minimalem distance
insertBefore wenn Cursor vor der Mitte (dx < 0 || dy < 0), sonst insertAfter
```

Das funktioniert ohne Annahmen über das CSS-Layout.

## Betroffene Dateien

| Datei | Änderung |
|---|---|
| `src/js/modules/manager/toolbar.inject.ts` | neuer `else if (action === "dragSectionEntries")` Block; `initDragDrop()`-Funktion; Import von `MOVE_ICON` |
| `src/js/modules/manager/manager.message.handlers.ts` | neuer `frameMessenger.on("sort-sections", ...)` Handler mit direktem `setMetaBatch`-Aufruf; Import von `setMetaBatch` |
| `src/js/modules/manager/toolbar-icons.ts` | keine Änderung (`MOVE_ICON` existiert bereits) |

## Was nicht geändert wird

- `edit-sections.js` — bleibt unverändert
- `toolbar-icons.ts` — `MOVE_ICON` ist bereits vorhanden
- sectionEntry-Toolbar-Konfiguration — keine neuen Actions erforderlich
- Preview-Reload nach Drop — bewusst weggelassen

## Offene Punkte / Entscheidungen

- Das MOVE_ICON-Handle wird vom section-Container-Init in die sectionEntry-Toolbar injiziert. Das setzt voraus, dass sectionEntry-Kinder mit `[data-cms-toolbar]` und `data-cms-type="sectionEntry"` (oder äquivalent) auffindbar sind. Die exakte Selektor-Logik wird in der Implementierung anhand des DOM-Outputs von `initToolbar` für sectionEntry-Elemente verifiziert.
