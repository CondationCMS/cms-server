# condation:add – Design

## Intent
Wer ein CondationCMS-Modul entwickelt, soll eine neue Extension (Typen aus `com.condation.cms.api.extensions` sowie die `ModuleLifeCycleExtension` des modules-framework, die für Modul-Entwickler relevant ist) per interaktiver Eingabe anlegen können, statt sie aus dem Beispielmodul zu kopieren.

## Goals
- Goal `condation:add` im `condation-maven-plugin`, interaktiv per JLine (aktuellste Version, derzeit 4.7.0).
- Der Benutzer wählt den Extension-Typ, gibt den Klassennamen ein und beantwortet die Typ-spezifischen Fragen (Shortcode-Name, Pfad, Operator …). Die erzeugte Klasse enthält direkt ein erstes, funktionierendes Element (z. B. einen `@ShortCode`).
- Die Klasse ist kompilierbar und liegt in `src/main/java/<paket>/<Name>.java`.
- Nicht-interaktiver Modus über Properties, damit Skripte und Tests ohne Terminal laufen.

## Non-goals
- Keine Registrierung außerhalb der Klasse (`@Extension` erzeugt den Service-Eintrag per Annotation-Processor).
- Kein Anlegen von Modulen, Sites oder Templates; kein Ändern bestehender Dateien.

## Constraints
- Das Plugin bleibt ein normales Maven-Plugin (JLine als Plugin-Abhängigkeit, kein Shading). Ob JLine 4 unter Maven 3.9 und 4 ein Terminal bekommt, wird beim Bauen geprüft; ohne Terminal fällt `add` mit klarer Meldung auf "use -Dcondation.add.*" zurück.
- Version wie Release (8.4.0), Java 25.

## Approaches
- A: Vorlagen als Java-Code im Plugin (Text-Blöcke je Typ), Prompt-Schicht hinter einem Interface (JLine bzw. skriptbar) – **gewählt**: kein Template-Framework nötig, voll testbar.
- B: Vorlagen als Ressourcen-Dateien mit Platzhaltern – verworfen: bei Typen mit Optionen (Transport, HTTP-Methode) wird die Logik in den Platzhaltern unübersichtlich.
- C: Archetype-Aufruf pro Extension – verworfen: Archetypes erzeugen Projekte, keine Dateien in bestehenden Projekten.

## Design (A)
- `AddMojo` (`@Mojo(name="add")`, benötigt Projekt). Parameter: `condation.add.type`, `condation.add.name`, `condation.add.package`, `condation.add.options` (`key=value,key=value`), `condation.add.overwrite`. Fehlt ein Wert und gibt es ein Terminal, wird er interaktiv abgefragt, sonst Fehler mit Hinweis.
- `Prompter` (Interface: `select`, `ask(label, default, validator)`, `confirm`). `JLinePrompter` (Terminal + LineReader, Auswahlliste mit Tab-Vervollständigung) und `ScriptedPrompter` (Werte aus den Properties).
- `ExtensionType` (Registry): `id`, Anzeigename, Extension-Point-Klasse, Liste der Fragen (`key`, Label, Default, Validierung) und `String render(ExtensionSpec)`.
- `ExtensionSpec` (Paket, Klassenname, Optionswerte).
- `ExtensionGenerator`: schreibt die Datei, legt Verzeichnisse an, fragt bei vorhandener Datei nach (oder `overwrite`). Klassenname: gültiger Java-Bezeichner, Default `My<Typ>Extension`.
- Paket-Default: häufigstes Paket der Klassen mit `@Extension` im Quellverzeichnis, sonst `groupId` des Projekts.
- Typen und Fragen (Annotation-Attribute aus den Quellen von `cms-api`/Beispielmodul):

| Typ | Fragen | Inhalt |
|---|---|---|
| Hook (`HookSystemRegisterExtensionPoint`) | Art (`action`/`filter`), Hook-Name, Scope (`REQUEST`/`APPLICATION`) | `@Action(value, scope)`-Methode bzw. `@Filter(value, scope)`-Methode (nimmt den Wert und gibt ihn zurück) |
| Shortcode (`RegisterShortCodesExtensionPoint`) | Shortcode-Name, Parameter-Name | `@ShortCode("name")`-Methode mit `@Param("param") String param` |
| Template-Funktion (`RegisterTemplateFunctionExtensionPoint`) | Funktions-Name, Parameter-Name | `@TemplateFunction("name")` mit `@Param("param") String param` |
| Template-Komponente (`RegisterTemplateComponentExtensionPoint`) | Komponenten-Name, Parameter-Name | `@TemplateComponent("name")` mit `@Param("param") String param` (Kommentar zeigt `Parameter` mit `_content`) |
| Realtime (`RealtimeExtensionPoint`) | Transport (SSE/WebSocket), Pfad | `@RealtimeEndpoint` |
| API-Handler (`APIHandlerExtensionPoint`) | Pfad, HTTP-Methode | `PathMapping` mit Handler |
| HTTP-Handler / Routes (`HttpHandlerExtensionPoint`, `HttpRoutesExtensionPoint`, `RoutesExtensionPoint`) | Pfad | `Mapping`/Route mit Handler |
| Template-Model (`TemplateModelExtendingExtensionPoint`) | Model-Schlüssel | `getModel()` |
| Content-Query-Operator | Operator-Name | `getOperator()`, `getPredicate()` |
| Varianten-Selektor | Id, Label | `id()`, `label()`, `select(...)` |
| Backup-Nachbearbeitung | – | `postProcess(Path)` |
| Cache-/Markdown-/Template-Engine-Provider | Name | `getName()`, Rest `UnsupportedOperationException("TODO")` |
| Modul-Lifecycle (`com.condation.modules.api.ModuleLifeCycleExtension<Context>`, modules-framework) | – | `init()`, `activate()`, `deactivate()` (wie im Archetype) |
| Server-Lifecycle, Server-Hook-Registrierung | – | `started()/stopped()` bzw. `register(HookSystem)` |

## Annotationen aus `com.condation.cms.api.annotations`
- Verwendet in den Stubs: `@Action`/`@Filter` (inkl. `Scope`), `@ShortCode`, `@TemplateFunction`, `@TemplateComponent`, `@Route`, `@Param` (typisierte Parameter statt `Parameter`/`ActionContext`).
- Nicht abgefragt: `namespace` (Default des Moduls passt), `priority` (Default 10), `@FeatureScope` (gehört an Feature-Klassen, keine Extension), `@Experimental` (Marker).

## Tests
- Je Typ: Die erzeugte Quelle wird mit `javax.tools.JavaCompiler` gegen `cms-api` und `modules-api` kompiliert (Test-Abhängigkeiten) und enthält die erfragten Werte.
- `ExtensionGenerator`: Pfad aus Paket, vorhandene Datei, ungültiger Klassenname.
- `ScriptedPrompter`/`AddMojo`: nicht-interaktiver Ablauf mit Properties; fehlendes Terminal ohne Properties → klare Fehlermeldung.
- `JLinePrompter`: mit gepiptem Eingabestrom (Dumb-Terminal) getestet.
- Doku: `documentation/maven-plugin.md`, README des Archetype.

## Left to the builder
- Genaue Annotation-Attribute und Signaturen je Typ (aus den Quellen), Wahl der JLine-Artefakte (4.x-Struktur), Reihenfolge der Fragen.
