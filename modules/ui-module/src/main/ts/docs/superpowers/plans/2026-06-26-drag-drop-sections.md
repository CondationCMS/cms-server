# Drag & Drop Section Sorting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sections im CMS-Preview per nativem HTML5 Drag & Drop umsortieren, mit Auto-Save on Drop und ohne Seiten-Reload.

**Architecture:** Ein neuer Action-Typ `"dragSectionEntries"` wird in `toolbar.inject.ts` verarbeitet: wenn er erkannt wird, initialisiert `initDragDrop()` natives DnD auf dem section-Container und injiziert Move-Handles in alle Kind-Elemente vom Typ `sectionEntry`. Nach dem Drop sendet der Preview-Frame per `frameMessenger` ein `"sort-sections"`-Kommando mit den neuen Index-Werten; ein neuer Handler im Manager-Frame ruft direkt `setMetaBatch()` auf.

**Tech Stack:** TypeScript, natives HTML5 Drag & Drop API, frameMessenger (bestehendes Messaging-System), `setMetaBatch` aus `rpc-content.ts`

## Global Constraints

- Nur Desktop-Browser (Chrome, Firefox, Safari, Edge aktuell) — kein Touch-Support
- Kein Seiten-Reload nach dem Drop (`reloadPreview()` wird nicht aufgerufen)
- Keine externen Libraries (kein SortableJS o.ä.)
- Layout-agnostisch: funktioniert für Flex-Row, Flex-Column und Grid
- `"orderSectionEntries"` (Modal) bleibt unverändert und weiterhin nutzbar
- Build-Command: `npm run build` (ruft `tsc` auf) im Verzeichnis `src/main/ts`

---

## File Map

| Datei | Aktion | Verantwortung |
|---|---|---|
| `src/js/modules/manager/toolbar.inject.ts` | Modify | Neuer `"dragSectionEntries"` Branch + `initDragDrop()`-Funktion |
| `src/js/modules/manager/manager.message.handlers.ts` | Modify | Neuer `"sort-sections"`-Handler mit `setMetaBatch` |

---

## Task 1: `sort-sections` Message Handler im Manager-Frame

**Files:**
- Modify: `src/js/modules/manager/manager.message.handlers.ts`

**Interfaces:**
- Consumes: `setMetaBatch` aus `@cms/modules/rpc/rpc-content.js` (bereits exportiert)
- Consumes: `frameMessenger.on(type, callback)` (bereits im File vorhanden)
- Produces: Handler reagiert auf `{ type: "sort-sections", payload: { updates: Array<{ uri: string, meta: { "layout.order": { type: "number", value: number } } }> } }`

- [ ] **Step 1: Import `setMetaBatch` hinzufügen**

In `manager.message.handlers.ts`, Zeile 24 (nach dem letzten Import), folgende Zeile einfügen:

```typescript
import { getContentNode, setMetaBatch } from '@cms/modules/rpc/rpc-content.js';
```

Achtung: Der bestehende Import von `getContentNode` in Zeile 24 muss dabei ersetzt werden:

```typescript
// vorher (Zeile 24):
import { getContentNode } from '@cms/modules/rpc/rpc-content.js';

// nachher:
import { getContentNode, setMetaBatch } from '@cms/modules/rpc/rpc-content.js';
```

- [ ] **Step 2: Handler nach dem `getContentNode`-Handler einfügen**

Nach dem `frameMessenger.on('getContentNode', ...)` Block (Zeile 184-196), vor der schließenden `}` von `initMessageHandlers`, folgenden Block einfügen:

```typescript
    frameMessenger.on('sort-sections', async (payload: any) => {
        await setMetaBatch({ updates: payload.updates });
    });
```

- [ ] **Step 3: Build ausführen und auf Fehler prüfen**

```bash
cd /pfad/zu/src/main/ts && npm run build
```

Erwartetes Ergebnis: Build erfolgreich, keine TypeScript-Fehler.

- [ ] **Step 4: Manuell verifizieren**

Prüfen dass im kompilierten Output `manager.message.handlers.js` der neue Handler vorhanden ist:

```bash
grep -n "sort-sections" src/js/modules/manager/manager.message.handlers.js
```

Erwartetes Ergebnis: Zeile mit `sort-sections` gefunden.

---

## Task 2: `initDragDrop()` und `"dragSectionEntries"` in `toolbar.inject.ts`

**Files:**
- Modify: `src/js/modules/manager/toolbar.inject.ts`

**Interfaces:**
- Consumes: `MOVE_ICON` aus `@cms/modules/manager/toolbar-icons` (bereits im File importierbar, muss zum Import hinzugefügt werden)
- Consumes: `frameMessenger` aus `@cms/modules/frameMessenger.js` (bereits importiert, Zeile 21)
- Produces: Funktion `initDragDrop(container: HTMLElement, sectionName: string): void`
- Produces: Neuer `else if (action === "dragSectionEntries")` Branch in `initToolbar()`

**Voraussetzung aus Task 1:** Handler für `"sort-sections"` muss im Manager registriert sein (Task 1 abgeschlossen).

### Wie sectionEntry-Kinder gefunden werden

`initToolbar()` wird für jeden Container aufgerufen, der `[data-cms-toolbar]` hat. Für `sectionEntry`-Elemente setzt es `container.classList.add("cms-ui-editable-sections")`. `initDragDrop()` sucht daher im **Parent** des section-Containers nach allen direkten Kinder-Elementen mit der Klasse `cms-ui-editable-sections`:

```
section-Container (hat "dragSectionEntries" Action)
  └── Kind-Element 1 [data-cms-toolbar type="sectionEntry"] → hat Klasse cms-ui-editable-sections
  └── Kind-Element 2 [data-cms-toolbar type="sectionEntry"] → hat Klasse cms-ui-editable-sections
```

Da `initDragDrop` via `requestAnimationFrame` aufgerufen wird, sind alle `initToolbar`-Aufrufe für die Kinder bereits abgeschlossen.

### Layout-agnostische Insert-Position

```
für jedes draggable-Kind k (außer dem gezogenen Element):
  rect = k.getBoundingClientRect()
  dx = event.clientX - (rect.left + rect.width / 2)
  dy = event.clientY - (rect.top + rect.height / 2)
  distance = Math.sqrt(dx*dx + dy*dy)

nächstes Element = Kind mit minimalem distance
wenn Cursor vor der Mitte des nächsten Elements (dy < 0 ODER (dy === 0 UND dx < 0))
  → insertBefore(dragged, nearest)
sonst
  → insertBefore(dragged, nearest.nextSibling)  // = insertAfter
```

- [ ] **Step 1: `MOVE_ICON` zum Import hinzufügen**

Zeile 22 in `toolbar.inject.ts` anpassen:

```typescript
// vorher:
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "@cms/modules/manager/toolbar-icons";

// nachher:
import { EDIT_ATTRIBUTES_ICON, EDIT_PAGE_ICON, MOVE_ICON, SECTION_ADD_ICON, SECTION_DELETE_ICON, SECTION_SORT_ICON, SECTION_UNPUBLISHED_ICON } from "@cms/modules/manager/toolbar-icons";
```

- [ ] **Step 2: `initDragDrop()` Funktion vor `initToolbar` einfügen**

Direkt vor `export const initToolbar = ...` (Zeile 136) folgende Funktion einfügen:

```typescript
const initDragDrop = (container: HTMLElement, sectionName: string) => {
    const draggableItems = Array.from(
        container.querySelectorAll<HTMLElement>(':scope > .cms-ui-editable-sections')
    );

    if (draggableItems.length === 0) {
        return;
    }

    let draggedEl: HTMLElement | null = null;

    draggableItems.forEach((item) => {
        item.setAttribute('draggable', 'true');

        // Move-Handle in die Toolbar des Items injizieren
        const itemToolbar = item.querySelector<HTMLElement>('.cms-ui-toolbar');
        if (itemToolbar) {
            const handle = document.createElement('button');
            handle.setAttribute('data-cms-drag-handle', '');
            handle.setAttribute('title', 'Drag to reorder');
            handle.innerHTML = MOVE_ICON;
            handle.style.cursor = 'grab';
            // mousedown/mouseup auf dem Handle steuert das draggable-Attribut,
            // damit nur das Handle das Ziehen auslöst
            handle.addEventListener('mousedown', () => {
                item.setAttribute('draggable', 'true');
            });
            itemToolbar.appendChild(handle);
        }

        item.addEventListener('dragstart', (e: DragEvent) => {
            draggedEl = item;
            item.style.opacity = '0.4';
            e.dataTransfer?.setData('text/plain', '');
        });

        item.addEventListener('dragend', () => {
            item.style.opacity = '';
            draggedEl = null;
        });
    });

    container.addEventListener('dragover', (e: DragEvent) => {
        e.preventDefault();
        if (!draggedEl) return;

        const siblings = Array.from(
            container.querySelectorAll<HTMLElement>(':scope > .cms-ui-editable-sections')
        ).filter(el => el !== draggedEl);

        if (siblings.length === 0) return;

        let nearest: HTMLElement = siblings[0];
        let nearestDist = Infinity;

        siblings.forEach(el => {
            const rect = el.getBoundingClientRect();
            const dx = e.clientX - (rect.left + rect.width / 2);
            const dy = e.clientY - (rect.top + rect.height / 2);
            const dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = el;
            }
        });

        const rect = nearest.getBoundingClientRect();
        const dy = e.clientY - (rect.top + rect.height / 2);
        const dx = e.clientX - (rect.left + rect.width / 2);
        const before = dy < 0 || (dy === 0 && dx < 0);

        if (before) {
            container.insertBefore(draggedEl, nearest);
        } else {
            container.insertBefore(draggedEl, nearest.nextSibling);
        }
    });

    container.addEventListener('drop', async (e: DragEvent) => {
        e.preventDefault();
        if (!draggedEl) return;

        const items = Array.from(
            container.querySelectorAll<HTMLElement>(':scope > .cms-ui-editable-sections')
        );

        const updates = items.map((el, index) => {
            const toolbarData = el.dataset.cmsToolbar ? JSON.parse(el.dataset.cmsToolbar) : {};
            return {
                uri: toolbarData.uri,
                meta: {
                    'layout.order': {
                        type: 'number',
                        value: index
                    }
                }
            };
        }).filter(u => u.uri);

        frameMessenger.send(window.parent, {
            type: 'sort-sections',
            payload: { updates }
        });
    });
};
```

- [ ] **Step 3: `"dragSectionEntries"` Branch in `initToolbar()` hinzufügen**

In der `toolbarDefinition.actions.forEach`-Schleife (nach dem `else if (action === "deleteSectionEntry")` Block, ca. Zeile 207) folgenden Block einfügen:

```typescript
        } else if (action === "dragSectionEntries") {
            // Kein Button — DnD wird nach dem ersten Render-Frame initialisiert,
            // damit alle sectionEntry-Toolbars bereits im DOM sind.
            const sectionName = toolbarDefinition.section || '';
            requestAnimationFrame(() => {
                initDragDrop(container, sectionName);
            });
        }
```

- [ ] **Step 4: Build ausführen**

```bash
cd /pfad/zu/src/main/ts && npm run build
```

Erwartetes Ergebnis: Build erfolgreich, keine TypeScript-Fehler.

- [ ] **Step 5: Manuell verifizieren — Handle-Injektion**

Im kompilierten Output prüfen:

```bash
grep -n "dragSectionEntries\|initDragDrop\|sort-sections" src/js/modules/manager/toolbar.inject.js
```

Erwartetes Ergebnis: Alle drei Strings gefunden.

---

## Task 3: Manuelle End-to-End-Verifikation

Kein automatisierter Test möglich (DnD erfordert Browser-Interaktion). Manuelle Schritte:

- [ ] **Step 1: Template mit `"dragSectionEntries"` konfigurieren**

In einem Test-Template die section-Toolbar auf den neuen Action-Typ umstellen:

```html
{{ ext.ui.toolbar("asection", "section", ["addSectionEntry", "dragSectionEntries"], { "section": "asection"}) | raw }}
```

- [ ] **Step 2: Vorschau öffnen und Toolbar-Hover prüfen**

Im CMS-Manager die Seite mit der konfigurierten Section aufrufen. Beim Hovern über ein sectionEntry-Element muss in dessen Toolbar das Move-Icon (`MOVE_ICON` — Kreuz-Pfeile) erscheinen.

- [ ] **Step 3: Drag & Drop testen**

Ein sectionEntry-Element am Move-Handle greifen und an eine andere Position ziehen. Beim Loslassen muss:
1. Das Element an der neuen Position im DOM verbleiben
2. Kein Seiten-Reload stattfinden
3. In den Browser DevTools (Network) ein RPC-Request `meta.set.batch` mit den aktualisierten `layout.order`-Werten sichtbar sein

- [ ] **Step 4: Persistenz prüfen**

Seite neu laden — die Sections müssen in der neuen Reihenfolge erscheinen (entsprechend der gespeicherten `layout.order`-Werte).

- [ ] **Step 5: Rückwärtskompatibilität prüfen**

Eine Section mit `"orderSectionEntries"` (Modal) in der Toolbar öffnen und sicherstellen, dass der Sortier-Dialog weiterhin funktioniert.
