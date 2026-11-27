# condation:add Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Goal `condation:add` erzeugt interaktiv (JLine) oder per Properties eine neue Extension-Klasse im Modul.

**Architecture:** Neues Paket `com.condation.cms.maven.add` im `condation-maven-plugin`. `ExtensionType`-Implementierungen rendern Java-Quelltext (Text-Blöcke); `ExtensionGenerator` schreibt die Datei; `Prompter` (JLine bzw. "kein Terminal") liefert fehlende Werte; `AddMojo` orchestriert.

**Tech Stack:** Java 25, Maven Plugin API, JLine 4.7.0 (`org.jline`, neueste Version), JUnit 5, AssertJ, `javax.tools.JavaCompiler` in Tests.

**Spec:** `docs/superpowers/specs/2026-11-25-condation-add-goal-design.md`

## Global Constraints

- Paket `com.condation.cms.maven.add`; Plugin-Version 8.4.0; Java 25.
- Goal `add` (`@Mojo(name="add", requiresDirectInvocation=true)`), erbt `AbstractCondationMojo` (`skip`, `project`). Parameter: `condation.add.type`, `condation.add.name`, `condation.add.package`, `condation.add.options` (`key=value,key=value`), `condation.add.overwrite` (Default false), `sourceDirectory` (`${project.build.sourceDirectory}`).
- Ziel: `<sourceDirectory>/<paket als Pfad>/<Name>.java`; Klassenname = gültiger Java-Bezeichner, Default `My<Typ-Camelcase>Extension`.
- Paket-Default: häufigstes Paket der Dateien mit `@Extension(`, sonst `groupId` des Projekts.
- Jede erzeugte Quelle enthält die erfragten Werte als korrekt escapte Java-String-Literale und muss ohne weitere Abhängigkeiten als `cms-api`, `modules-api` und Jetty kompilieren.
- Ohne Terminal und mit fehlenden Werten: `MojoExecutionException` mit Hinweis auf die `-Dcondation.add.*`-Properties.
- Typ-IDs (exakt): `hook`, `shortcode`, `template-function`, `template-component`, `realtime`, `api-handler`, `http-handler`, `http-routes`, `routes`, `template-model`, `query-operator`, `variant-selector`, `backup-post-processing`, `cache-provider`, `markdown-renderer`, `template-engine`, `server-lifecycle`, `server-hook`, `module-lifecycle`.
- Neue Dateien: AGPL-Header fügt das Lizenz-Plugin im Build automatisch ein; Commits macht der Benutzer selbst (nicht committen).

## Review Focus

- Parameter-Name ungültig (`my param`, `class`) → abgelehnt (Task 1/2).
- Wert mit `"` oder `\` (z. B. Shortcode-Name `a"b`) → erzeugte Klasse kompiliert weiterhin (Task 2/3).
- Klassenname ungültig (`1Foo`, `foo bar`, leer, Schlüsselwort) → klare Fehlermeldung, keine Datei (Task 1/5).
- Zieldatei existiert → nicht überschreiben ohne Bestätigung/`overwrite` (Task 5).
- Kein Terminal und fehlende Werte → Fehler mit Property-Hinweis statt Hänger (Task 6).
- Strg+C / EOF im Prompt → Abbruch ohne Datei und ohne Stacktrace (Task 7).
- Projekt ohne `src/main/java` → Verzeichnis wird angelegt (Task 5).

---

### Task 1: Grundlagen (Abhängigkeiten, Modelle, Namen, Compile-Helper)

**Files:**
- Modify: `condation-maven-plugin/pom.xml` (Abhängigkeiten)
- Create: `.../maven/add/ExtensionSpec.java`, `Question.java`, `ExtensionType.java`, `Names.java`
- Test: `.../maven/add/NamesTest.java`, Test-Util `.../maven/add/CompileHelper.java`

**Interfaces:**
- Produces:
  - `public record ExtensionSpec(String packageName, String className, Map<String,String> options) { public String option(String key) }` (wirft `IllegalArgumentException` bei fehlendem Key)
  - `public record Question(String key, String label, String defaultValue, List<String> choices)` (`choices` leer = Freitext)
  - `public interface ExtensionType { String id(); String label(); List<Question> questions(); String render(ExtensionSpec spec); }`
  - `public final class Names { static String requireClassName(String); static String requireIdentifier(String) /* z. B. Parameter-Name */; static String requirePackage(String); static String javaString(String value) /* escaped, ohne Anführungszeichen */; static String defaultClassName(ExtensionType type) }` – `require*` werfen `IllegalArgumentException` mit sprechender Meldung.
  - Test-Util `CompileHelper.compile(String packageName, String className, String source)` → wirft `AssertionError` mit Compiler-Diagnostics, wenn die Quelle nicht kompiliert (Classpath = `java.class.path`, `-proc:none`).

- [ ] **Step 1: Failing Tests** `NamesTest`: `should_accept_valid_class_name`, `should_reject_when_class_name_starts_with_digit`, `should_reject_when_class_name_contains_space`, `should_reject_when_class_name_is_java_keyword` (`class`), `should_reject_empty_class_name`, `should_accept_nested_package`, `should_reject_package_with_keyword_segment` (`com.int.x`), `should_escape_quotes_and_backslashes_in_java_string` (`a"b\c` → `a\"b\\c`), `should_build_default_class_name_from_type_id` (`template-function` → `MyTemplateFunctionExtension`).
- [ ] **Step 2: Run** `./mvnw -pl condation-maven-plugin test -Dtest=NamesTest` → FAIL (Klassen fehlen).
- [ ] **Step 3: pom** – Abhängigkeiten ergänzen: `org.jline` JLine 4.7.0 (zu verwendende Artefakte nach Prüfung der 4.x-Struktur wählen, Terminal + Reader + Provider für macOS/Linux/Windows), Test-Scope: `com.condation.cms:cms-api:${project.version}`, `com.condation.modules.framework:modules-api:${modules-framework.version}`. **Implement** die Klassen laut Interfaces; `CompileHelper` mit `ToolProvider.getSystemJavaCompiler()` und In-Memory-Quelle.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS, `NamesTest` grün, bestehende Tests grün.

### Task 2: Typen mit Annotationen (hook, shortcode, template-function, template-component, realtime, module-lifecycle)

**Files:**
- Create: `.../maven/add/types/HookType.java`, `ShortCodeType.java`, `TemplateFunctionType.java`, `TemplateComponentType.java`, `RealtimeType.java`, `ModuleLifeCycleType.java`, `ExtensionTypes.java`
- Test: `.../maven/add/types/AnnotationTypesTest.java`

**Interfaces:**
- Consumes: `ExtensionType`, `ExtensionSpec`, `Question`, `Names`, `CompileHelper` (Task 1).
- Produces: `public final class ExtensionTypes { public static List<ExtensionType> all(); public static Optional<ExtensionType> byId(String id); }` (alle Typen aus allen Tasks; in dieser Task nur die 6).

Fragen und Inhalt (alles `@Extension(<ExtensionPoint>.class)`, Klasse erbt vom Extension-Point, Imports exakt nach Beispielmodul/`cms-api`):
- `hook`: Fragen `kind` (Auswahl `action`,`filter`, Default `action`), `hook` (Default `my/hook`), `scope` (Auswahl `REQUEST`,`APPLICATION`, Default `REQUEST`) → `HookSystemRegisterExtensionPoint`; `action`: `@Action(value="<hook>", scope=Scope.<scope>) public String ...()`; `filter`: `@Filter(value="<hook>", scope=Scope.<scope>) public String ...(String value) { return value; }` (`com.condation.cms.api.annotations.{Action,Filter,Scope}`).
- `shortcode`: Fragen `name` (Default `hello`), `param` (gültiger Java-Bezeichner, Default `name`) → `@ShortCode("<name>") public String ...(@Param("<param>") String <param>)` (`RegisterShortCodesExtensionPoint`, `com.condation.cms.api.annotations.Param`), gibt `"<b>Hello " + <param> + "</b>"`-artigen HTML-String zurück.
- `template-function`: Fragen `name`, `param` → `@TemplateFunction("<name>") public Object ...(@Param("<param>") String <param>)`.
- `template-component`: Fragen `name`, `param` → `@TemplateComponent("<name>") public String ...(@Param("<param>") String <param>)`; ein Kommentar zeigt die Alternative `Parameter parameter` (`_content` = Inhalt der Komponente).
- `realtime`: `transport` (Auswahl `SSE`,`WEBSOCKET`, Default `SSE`), `path` (Default `events`) → `@RealtimeEndpoint(transport=..., path="<path>")`; SSE: `(SseConnection c)` mit `c.send("connected", "...")`; WebSocket: `(WebSocketConnection c)` mit `c.onText(m -> c.sendText("echo: " + m))` (`com.condation.cms.api.realtime.*`).
- `module-lifecycle`: keine Fragen → `@Extension(ModuleLifeCycleExtension.class) extends ModuleLifeCycleExtension<Context>` mit `init()`, `activate()`, `deactivate()`, die über `System.Logger` loggen (wie `ModuleLifeCycle` im Archetype).

- [ ] **Step 1: Failing Tests** in `AnnotationTypesTest` (parametrisiert über diese 6 Typen): `should_compile_when_default_values_used`, `should_contain_given_value_when_rendered` (z. B. Shortcode-Name `my-tag` taucht als `@ShortCode("my-tag")` auf), `should_compile_when_value_contains_quote_and_backslash` (Wert `a"b\c`), `should_render_websocket_variant_when_transport_websocket`, `should_render_filter_when_kind_filter` (enthält `@Filter(` und `(String value)`), `should_use_param_annotation_with_given_parameter_name` (Shortcode/Funktion/Komponente), `should_reject_invalid_parameter_name`; plus `ExtensionTypesTest.should_find_type_by_id`.
- [ ] **Step 2: Run** `-Dtest=AnnotationTypesTest` → FAIL.
- [ ] **Step 3: Implement** die Typen (Text-Blöcke, Werte via `Names.javaString`), `ExtensionTypes` mit Liste.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS.

### Task 3: Handler- und Mapping-Typen (api-handler, http-handler, http-routes, routes, template-model, query-operator, variant-selector)

**Files:**
- Create: `.../maven/add/types/ApiHandlerType.java`, `HttpHandlerType.java`, `HttpRoutesType.java`, `RoutesType.java`, `TemplateModelType.java`, `QueryOperatorType.java`, `VariantSelectorType.java`
- Modify: `ExtensionTypes.java` (registrieren)
- Test: `.../maven/add/types/HandlerTypesTest.java`

**Interfaces:**
- Consumes/Produces: wie Task 2; `ExtensionTypes.all()` enthält danach 13 Typen.

Fragen und Inhalt:
- `api-handler`: `path` (Default `/my-api`), `method` (Auswahl `GET`,`POST`,`PUT`,`DELETE`, Default `GET`) → `APIHandlerExtensionPoint.getMapping()` mit `PathMapping.add(PathSpec.from(path), method, handler)`; Handler als Lambda (`com.condation.cms.api.extensions.http.HttpHandler`), schreibt einen Text.
- `http-handler`: `path` (Default `/my-path`) → `HttpHandlerExtensionPoint.getMapping()` mit `Mapping.add(PathSpec.from(path), lambda)` (Imports wie `ExampleJettyHttpHandlerExtension`).
- `http-routes`: `path` (Default `/my-route`) → `HttpRoutesExtensionPoint` wie `ExampleJettyRoutesHandlerExtension`.
- `routes`: `path` (Default `/my-route`), `method` (Default `GET`) → `RoutesExtensionPoint` mit `@Route(value="<path>", method="<method>") public boolean handle(Request, Response, Callback)`.
- `template-model`: `key` (Default `myModel`) → `getModel()` liefert `Map.of("<key>", ...)`.
- `query-operator`: `operator` (Default `my-operator`) → `getOperator()`, `getPredicate()` (`(nodeValue, value) -> Objects.equals(nodeValue, value)`).
- `variant-selector`: `id` (Default `my-variant`), `label` (Default `My variant`) → `id()`, `label()`, `select(...)` liefert `VariantSelection.canonical()`.

- [ ] **Step 1: Failing Tests** (parametrisiert): kompiliert mit Defaults; enthält Wert; kompiliert mit `a"b\c`; `api-handler` mit `POST` enthält `"POST"`; ungültige Methode wird von der Auswahl-Validierung im Mojo abgefangen (Task 6), hier nur Rendern.
- [ ] **Step 2: Run** `-Dtest=HandlerTypesTest` → FAIL.
- [ ] **Step 3: Implement** die sieben Typen, registrieren.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS.

### Task 4: Übrige Typen (backup-post-processing, cache-provider, markdown-renderer, template-engine, server-lifecycle, server-hook)

**Files:**
- Create: `.../maven/add/types/BackupPostProcessingType.java`, `CacheProviderType.java`, `MarkdownRendererType.java`, `TemplateEngineType.java`, `ServerLifecycleType.java`, `ServerHookType.java`
- Modify: `ExtensionTypes.java`
- Test: `.../maven/add/types/OtherTypesTest.java`

**Interfaces:** `ExtensionTypes.all()` enthält danach alle 19 Typen mit den exakten IDs aus Global Constraints (Test prüft die ID-Liste).

Inhalt:
- `backup-post-processing`: keine Fragen → `postProcess(Path backupFile)` mit Kommentar-Rumpf.
- `cache-provider`, `markdown-renderer`, `template-engine`: Frage `name` (Defaults `my-cache`, `my-markdown`, `my-engine`) → `getName()` liefert den Wert; zweite Methode (`getCacheProvider()`, `getRenderer()`, `getTemplateEngine()`) wirft `UnsupportedOperationException("TODO")`.
- `server-lifecycle`: `ServerLifecycleExtensionPoint` mit `started()`/`stopped()`; `server-hook`: `ServerHookSystemRegisterExtensionPoint` mit `register(HookSystem)`.

- [ ] **Step 1: Failing Tests:** `should_have_all_expected_type_ids`; je Typ kompiliert mit Defaults; Name-Wert enthalten; Escape-Test.
- [ ] **Step 2: Run** `-Dtest=OtherTypesTest` → FAIL.
- [ ] **Step 3: Implement** und registrieren.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS.

### Task 5: ExtensionGenerator und PackageDetector

**Files:**
- Create: `.../maven/add/ExtensionGenerator.java`, `PackageDetector.java`
- Test: `.../maven/add/ExtensionGeneratorTest.java`, `PackageDetectorTest.java`

**Interfaces:**
- Consumes: `ExtensionType`, `ExtensionSpec`, `Names`.
- Produces:
  - `ExtensionGenerator.write(Path sourceDirectory, ExtensionType type, ExtensionSpec spec, boolean overwrite) throws IOException` → `Path` der Datei; `FileAlreadyExistsException` wenn vorhanden und `!overwrite`; legt Verzeichnisse an.
  - `PackageDetector.detect(Path sourceDirectory, String groupId) -> String`.

- [ ] **Step 1: Failing Tests:** `should_write_file_into_package_directory`, `should_create_missing_source_directory`, `should_fail_when_file_exists_and_overwrite_false`, `should_overwrite_when_flag_set`, `should_reject_invalid_class_name_without_writing` (keine Datei, `IllegalArgumentException`), `PackageDetector`: `should_use_most_common_package_of_extension_classes`, `should_fall_back_to_group_id_when_no_extensions`, `should_ignore_files_without_extension_annotation`, `should_use_group_id_when_source_directory_missing`.
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement.** `PackageDetector` liest nur `*.java` und deren `package`-Zeile (Regex), keine Parser-Bibliothek.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS.

### Task 6: Prompter-Schnittstelle und AddMojo

**Files:**
- Create: `.../maven/add/Prompter.java`, `UnavailablePrompter.java`, `AddMojo.java` (in `.../maven/`, Paket `com.condation.cms.maven`)
- Test: `.../maven/AddMojoTest.java`

**Interfaces:**
- Produces:
  - `public interface Prompter { String select(String label, List<String> choices, String defaultValue) throws IOException; String ask(String label, String defaultValue, Predicate<String> validator, String errorMessage) throws IOException; boolean confirm(String label, boolean defaultValue) throws IOException; }` – Abbruch durch den Benutzer: `UserAbortException` (unchecked, eigene Klasse).
  - `UnavailablePrompter(String hint)` – jede Methode wirft `MojoExecutionException`-fähige `IllegalStateException` mit `hint`.
  - `AddMojo` (`Prompter` per Feld/Methode `createPrompter()` überschreibbar für Tests).

Ablauf `execute()`: skip → Typ (Property oder `select` aus `ExtensionTypes.all()` ids) → Name (Property oder `ask` mit Default + `Names.requireClassName`) → Paket (Property oder `ask` mit `PackageDetector`-Default) → je `Question` Wert aus `condation.add.options` oder `ask`/`select` (bei `choices` Validierung gegen die Auswahl, case-insensitive → Großschreibung der Auswahl) → bei vorhandener Datei `confirm` (oder `overwrite`) → `ExtensionGenerator.write` → Log mit relativem Pfad. `UserAbortException` → `MojoExecutionException("Aborted")` ohne Datei.

- [ ] **Step 1: Failing Tests** (Test-Prompter als Fake-Implementierung): `should_create_shortcode_class_when_all_values_given_as_properties`, `should_ask_for_missing_values_with_prompter`, `should_fail_with_property_hint_when_no_terminal_and_value_missing`, `should_fail_when_unknown_type`, `should_fail_when_choice_value_invalid` (`method=FOO`), `should_not_overwrite_when_user_declines_confirmation`, `should_do_nothing_when_skip_set`, `should_parse_options_with_multiple_pairs` (`a=1,b=2`).
- [ ] **Step 2: Run** `-Dtest=AddMojoTest` → FAIL.
- [ ] **Step 3: Implement** Mojo und Prompter-Typen; `createPrompter()` liefert `UnavailablePrompter` (JLine kommt in Task 7).
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin test` → PASS; `plugin.xml` enthält das Goal `add`.

### Task 7: JLinePrompter, Terminal-Erkennung, Doku, Gesamtprüfung

**Files:**
- Create: `.../maven/add/JLinePrompter.java`
- Modify: `AddMojo.java` (`createPrompter()`), `documentation/maven-plugin.md`, `cms-module-archetype/README.md`
- Test: `.../maven/add/JLinePrompterTest.java`

**Interfaces:**
- Consumes: `Prompter`, `UserAbortException`.
- Produces: `JLinePrompter(Terminal terminal) implements Prompter, AutoCloseable` mit `Terminal`-Factory `static JLinePrompter system() throws IOException` (nur wenn `System.console() != null && System.console().isTerminal()`, sonst `AddMojo` → `UnavailablePrompter`).

- [ ] **Step 1: Failing Tests** mit Dumb-Terminal und gepiptem Eingabestrom (`TerminalBuilder.builder().streams(in,out).type("dumb")`): `should_return_default_when_enter_pressed`, `should_return_typed_value`, `should_ask_again_when_validator_rejects`, `should_return_selected_choice`, `should_throw_abort_when_end_of_input`, `should_confirm_with_default_when_enter_pressed`.
- [ ] **Step 2: Run** `-Dtest=JLinePrompterTest` → FAIL.
- [ ] **Step 3: Implement** mit `LineReaderBuilder` (`StringsCompleter` für `select`), `UserInterruptException`/`EndOfFileException` → `UserAbortException`; `AddMojo.createPrompter()` nutzt `JLinePrompter.system()`, sonst `UnavailablePrompter` mit Hinweis auf `-Dcondation.add.type/name/package/options`.
- [ ] **Step 4: Doku** `maven-plugin.md` (Goal, Properties, Typ-Liste) und Archetype-README (`mvn condation:add`) ergänzen.
- [ ] **Step 5: Gesamtprüfung:** `./mvnw -pl condation-maven-plugin verify` grün; Plugin installieren (`./mvnw -q install -DskipTests -pl condation-maven-plugin`); Archetype-Projekt generieren und `mvn condation:add -Dcondation.add.type=shortcode -Dcondation.add.name=HelloTag -Dcondation.add.options=name=hello` im Root ausführen → Datei im `module`-Projekt, `mvn package` baut; interaktiv mit `script -q /dev/null` (pty) einmal durchspielen und das Ergebnis im Ledger festhalten.
