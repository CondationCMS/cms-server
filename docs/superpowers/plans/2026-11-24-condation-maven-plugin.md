# condation-maven-plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Maven-Plugin mit den Goals `condation:start`, `condation:stop`, `condation:run`, das den CMS-Server als separaten Java-Prozess gegen ein vom Entwickler angelegtes Home startet und die Modul-Zip dorthin installiert.

**Architecture:** Neues Reactor-Modul `condation-maven-plugin` (Packaging `maven-plugin`). Kleine, einzeln testbare Klassen (`ModuleInstaller`, `ServerPort`, `ServerProcess`, `ClasspathResolver`), die von drei dünnen Mojos genutzt werden. Der Server läuft in einem geforkten `java`-Prozess mit `-Dcms.home=<home>` und `com.condation.cms.cli.CMSCli server start|stop`.

**Tech Stack:** Java 25, Maven 3.9 plugin API (`maven-plugin-api`, `maven-plugin-annotations`, `maven-core`), Maven Resolver für den Classpath, `org.tomlj:tomlj` (im Parent verwaltet) für `server.toml`, JUnit 5 + AssertJ.

**Spec:** `docs/superpowers/specs/2026-11-24-condation-maven-plugin-design.md`

## Global Constraints

- Reactor-Modul `condation-maven-plugin`, groupId `com.condation.cms`, Parent `cms-parent` Version `8.4.0`, Packaging `maven-plugin`; Goal-Prefix `condation`.
- Server läuft nie im Maven-Prozess (`System.exit` im Server-Start).
- `cmsHome` Default `${project.basedir}/cms-home`; fehlt das Verzeichnis, Abbruch mit klarer Meldung, das Plugin legt es nicht an.
- `cmsVersion` Default = Plugin-Version; `startTimeout` Default 60 (Sekunden); Parameter `moduleZip`, `installModule` (Default true), `jvmArgs`, `skip`.
- Modul-Zip-Struktur `<id>/module.properties` + `<id>/libs/*.jar`; Installation nach `<home>/modules/<id>/`, alter Ordner wird vorher gelöscht.
- Erfolg beim Start = `cms.pid` im Home vorhanden UND HTTP-Port aus `server.toml` (`[server] port`) nimmt Verbindungen an.
- Neue Java-Dateien bekommen den AGPL-Header (license-maven-plugin im Parent fügt ihn beim Build automatisch ein).
- Version wie Release (wird mit dem Parent inkrementiert).

## Review Focus

- Home existiert nicht → Fehlermeldung mit Pfad, kein Prozess gestartet (Task 5).
- Modul-Zip fehlt (Modul nicht gebaut) → Fehlermeldung "run `mvn package` first" statt NPE (Task 2/5).
- Zip enthält Pfad mit `..` (Zip-Slip) → Abbruch, nichts außerhalb von `modules/` geschrieben (Task 2).
- `server.toml` ohne `[server] port` oder fehlt → klare Fehlermeldung bzw. Default 2020? Entscheidung: Fehler, kein Raten (Task 3).
- Server beendet sich beim Start (z. B. fehlende Themes, Exit-Code ≠ 0) → `start` bricht sofort mit Exit-Code und Verweis auf Logdatei ab, wartet nicht bis Timeout (Task 4).
- Bereits laufender Server (`cms.pid` lebt) → `start` bricht mit Hinweis auf `condation:stop` ab (Task 5).

---

### Task 1: Modul-Skeleton und Reactor-Eintrag

**Files:**
- Create: `condation-maven-plugin/pom.xml`
- Modify: `pom.xml` (Reactor: `<module>condation-maven-plugin</module>` nach `cms-test-server`)
- Create: `condation-maven-plugin/src/test/java/com/condation/cms/maven/SkeletonTest.java`

**Interfaces:**
- Produces: Maven-Modul `com.condation.cms:condation-maven-plugin:8.4.0`, Package `com.condation.cms.maven`.

- [ ] **Step 1: pom.xml anlegen.** Parent `cms-parent` 8.4.0, Packaging `maven-plugin`, `<name>CondationCMS Maven Plugin</name>`. Dependencies: `maven-plugin-api`, `maven-core`, `maven-resolver-api`/`-util` (alle `provided`, Version 3.9.x / 1.9.x aus `~/.m2` bzw. in einer `properties`-Sektion des Moduls pinnen), `maven-plugin-annotations` (`provided`), `org.tomlj:tomlj` (Version aus Parent), Test: `junit-jupiter`, `org.assertj:assertj-core` (falls im Parent verwaltet, sonst Version pinnen). Build: `maven-plugin-plugin` (4.x oder 3.15.x) mit Execution `descriptor`.
- [ ] **Step 2: Reactor-Eintrag** in `pom.xml` ergänzen.
- [ ] **Step 3: Failing Test** `SkeletonTest.should_load_when_module_is_built()` – `assertThat(true).isTrue()`; Zweck: Modul wird gebaut und Tests laufen.
- [ ] **Step 4: Verify** `./mvnw -pl condation-maven-plugin -am verify -DskipITs` → BUILD SUCCESS, `target/classes/META-INF/maven/plugin.xml` existiert.
- [ ] **Step 5: Test löschen** (er wird durch echte Tests ersetzt) und committen: `git add pom.xml condation-maven-plugin && git commit -m "feat: add condation-maven-plugin skeleton"`.

### Task 2: ModuleInstaller

**Files:**
- Create: `condation-maven-plugin/src/main/java/com/condation/cms/maven/ModuleInstaller.java`
- Test: `condation-maven-plugin/src/test/java/com/condation/cms/maven/ModuleInstallerTest.java`

**Interfaces:**
- Produces: `public final class ModuleInstaller { public static String install(Path zip, Path cmsHome) throws IOException }` – liefert die Modul-ID (Name des Top-Level-Ordners der Zip). Wirft `IOException` mit sprechender Nachricht, wenn `zip` nicht existiert ("Module zip not found: <path> – run `mvn package` first") oder ein Eintrag aus `<id>/` ausbricht.

- [ ] **Step 1: Tests** (Zips werden im Test mit `ZipOutputStream` in `@TempDir` erzeugt):
  - `should_extract_module_into_home_modules_when_zip_valid` – Zip mit `m1/module.properties`, `m1/libs/a.jar`; Ergebnis `"m1"`, Dateien liegen unter `<home>/modules/m1/...`.
  - `should_replace_existing_module_folder_when_installed_again` – vorab `<home>/modules/m1/old.txt`; nach Install existiert `old.txt` nicht mehr.
  - `should_fail_with_hint_when_zip_missing` – Nachricht enthält `mvn package`.
  - `should_reject_entry_escaping_module_folder_when_zip_slip` – Eintrag `m1/../../evil.txt` → `IOException`, `<home>/evil.txt` und `<tmp>/evil.txt` existieren nicht.
  - `should_fail_when_zip_has_multiple_top_level_folders`.
- [ ] **Step 2: Run** `./mvnw -pl condation-maven-plugin test -Dtest=ModuleInstallerTest` → FAIL (Klasse fehlt).
- [ ] **Step 3: Implement** `ModuleInstaller.install`: Top-Level-Ordner aus erstem Eintrag bestimmen, Zielordner rekursiv löschen, Einträge nach `normalize()` gegen `<home>/modules/<id>` prüfen (`startsWith`), kopieren.
- [ ] **Step 4: Run** → PASS.
- [ ] **Step 5: Commit** `feat: install module zip into cms home`.

### Task 3: ServerPort (server.toml)

**Files:**
- Create: `.../maven/ServerPort.java`
- Test: `.../maven/ServerPortTest.java`

**Interfaces:**
- Produces: `public final class ServerPort { public static int read(Path cmsHome) throws IOException }` – liest `<home>/server.toml`, Schlüssel `server.port`. `IOException` mit Pfad in der Nachricht, wenn Datei fehlt, nicht parsebar oder Port fehlt.

- [ ] **Step 1: Tests:** `should_return_port_when_server_toml_has_server_port` (`[server]\nport = 2020` → 2020), `should_fail_when_server_toml_missing`, `should_fail_when_port_not_defined`.
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement** mit `org.tomlj.Toml.parse`, `result.getLong("server.port")`.
- [ ] **Step 4: Run** → PASS.
- [ ] **Step 5: Commit** `feat: read server port from server.toml`.

### Task 4: ServerProcess (Prozess starten, warten, stoppen)

**Files:**
- Create: `.../maven/ServerProcess.java`
- Test: `.../maven/ServerProcessTest.java`
- Test-Fixture: `condation-maven-plugin/src/test/java/com/condation/cms/maven/FakeServer.java` (eigene `main`, die `cms.home` liest, `cms.pid` schreibt – Format wie `CLIServerUtils.writePidFile`: Properties `pid`, `startedAt` – und einen `ServerSocket` auf dem Port aus `server.toml` öffnet; Argument `exit1` beendet sich sofort mit Code 1).

**Interfaces:**
- Consumes: `ServerPort.read(Path)` (Task 3).
- Produces:
  - `public record ServerLaunch(Path cmsHome, List<Path> classpath, String mainClass, List<String> jvmArgs, Path logFile /*null = inherit IO*/)`
  - `public final class ServerProcess` mit
    - `static Process launch(ServerLaunch l, String... cliArgs) throws IOException` – `java` aus `System.getProperty("java.home")`, `-Dcms.home=<abs home>`, jvmArgs, `-cp`, mainClass, cliArgs; Arbeitsverzeichnis = Home; Ausgabe in `logFile` (append) oder `inheritIO()`.
    - `static void awaitReady(Process p, Path cmsHome, Duration timeout, Path logFile) throws MojoExecutionException` – pollt alle 200 ms; Erfolg wenn `cms.pid` existiert und Port erreichbar; sofortiger Abbruch mit Exit-Code und Logdatei-Pfad, wenn `p` vor Bereitschaft endet; Timeout-Fehler nennt Logdatei.
    - `static boolean isRunning(Path cmsHome)` – `cms.pid` vorhanden, `pid` lebt (`ProcessHandle.of`).
    - `static void stop(ServerLaunch l, Duration timeout) throws IOException, InterruptedException` – startet `launch(l, "server","stop")`, wartet auf Ende; danach wartet bis der Server-Prozess weg ist; Fallback: `ProcessHandle#destroy` auf PID aus `cms.pid`, nach Timeout `destroyForcibly`.

- [ ] **Step 1: Tests** mit `FakeServer` als `mainClass` und Test-Classpath (`System.getProperty("java.class.path")`):
  - `should_be_ready_when_pid_file_and_port_available`
  - `should_fail_fast_with_exit_code_when_process_ends_before_ready` (`exit1`; Dauer < Timeout, Nachricht enthält `1` und Logdatei-Pfad)
  - `should_fail_when_timeout_exceeded` (FakeServer-Modus `hang`: schreibt nie die PID)
  - `should_report_running_when_pid_alive` / `should_report_not_running_when_no_pid_file`
  - `should_terminate_process_when_stop_called` (FakeServer ignoriert `server stop`; Fallback beendet ihn).
- [ ] **Step 2: Run** `-Dtest=ServerProcessTest` → FAIL.
- [ ] **Step 3: Implement** wie in den Signaturen beschrieben.
- [ ] **Step 4: Run** → PASS.
- [ ] **Step 5: Commit** `feat: launch and stop cms server process`.

### Task 5: Mojos `start`, `stop`, `run` und Classpath

**Files:**
- Create: `.../maven/AbstractCondationMojo.java` (gemeinsame Parameter + `ServerLaunch`-Aufbau + Home-/Skip-Prüfung)
- Create: `.../maven/ClasspathResolver.java`
- Create: `.../maven/StartMojo.java`, `StopMojo.java`, `RunMojo.java`
- Test: `.../maven/AbstractCondationMojoTest.java`

**Interfaces:**
- Consumes: `ModuleInstaller.install(Path, Path)`, `ServerPort`, `ServerProcess`, `ServerLaunch`.
- Produces:
  - `ClasspathResolver.resolve(RepositorySystem, RepositorySystemSession, List<RemoteRepository>, String cmsVersion) -> List<Path>` – löst `com.condation.cms:cms-server:<cmsVersion>` samt Runtime-Abhängigkeiten auf.
  - `AbstractCondationMojo` mit Parametern: `@Parameter(property="condation.home", defaultValue="${project.basedir}/cms-home") File cmsHome`, `@Parameter(property="condation.version", defaultValue="${plugin.version}") String cmsVersion`, `@Parameter(property="condation.moduleZip", defaultValue="${project.build.directory}/${project.build.finalName}-bin.zip") File moduleZip`, `@Parameter(property="condation.installModule", defaultValue="true") boolean installModule`, `@Parameter(property="condation.startTimeout", defaultValue="60") int startTimeout`, `@Parameter(property="condation.jvmArgs") List<String> jvmArgs`, `@Parameter(property="condation.skip", defaultValue="false") boolean skip`; geschützte Methode `Path requireHome() throws MojoExecutionException`.
  - Goals: `start` (`@Mojo(name="start")`, Log nach `<home>/logs/plugin-server.out`), `stop`, `run` (Vordergrund, `inheritIO`, Shutdown-Hook ruft `ServerProcess.stop`, `process.waitFor()`).
  - Fehlerprüfung in `start`/`run` in dieser Reihenfolge: skip → Home vorhanden → kein laufender Server (Hinweis `condation:stop`) → optional Modul installieren → Classpath → Start.

- [ ] **Step 1: Tests** für `requireHome()`: `should_fail_with_path_when_home_missing`, `should_return_home_when_directory_exists`; Test, dass `start` bei fehlendem Home keinen Prozess startet (Mojo per Konstruktion/Reflection mit gesetzten Feldern, `ServerProcess` wird nicht erreicht → Exception vorher).
- [ ] **Step 2: Run** → FAIL.
- [ ] **Step 3: Implement** die Klassen; `RepositorySystem`/`RepositorySystemSession`/`remoteRepos` per `@Component`/`@Parameter(defaultValue="${repositorySystemSession}", readonly=true)`/`${project.remotePluginRepositories}`.
- [ ] **Step 4: Run** `./mvnw -pl condation-maven-plugin verify` → PASS; `plugin.xml` enthält die drei Goals.
- [ ] **Step 5: Commit** `feat: add start, stop and run goals`.

### Task 6: End-to-End mit Beispielmodul und Doku

**Files:**
- Create: `condation-maven-plugin/src/it/start-stop/` (maven-invoker-plugin-Projekt: `pom.xml`, `cms-home/server.toml` mit eigenem Port, `cms-home/hosts/`/`themes/` minimal, ein vorgebautes `target/<id>-bin.zip` oder Assembly im IT-Projekt, `invoker.properties`, `verify.groovy`)
- Modify: `condation-maven-plugin/pom.xml` (maven-invoker-plugin, Profil/Phase `integration-test`)
- Create: `documentation/maven-plugin.md`; Verweis in `documentation/index.md`

**Interfaces:**
- Consumes: Goals aus Task 5.

- [ ] **Step 1: IT** `mvn condation:start condation:stop` – `verify.groovy` prüft: Modul wurde nach `cms-home/modules/<id>/` installiert, `cms.pid` existiert nach start und ist nach stop weg, HTTP-Port war erreichbar (Start-Goal wäre sonst gescheitert).
- [ ] **Step 2: Run** `./mvnw -pl condation-maven-plugin verify` → IT passed. Schlägt der Server-Start im IT wegen fehlendem Demo-Host/Theme fehl: IT-Home so minimal wie nötig halten (kein Theme-Bedarf in `site.toml`).
- [ ] **Step 3: Doku** `documentation/maven-plugin.md`: Einbindung ins Modul-Pom (`pluginManagement`, Beispiel mit `pre-integration-test`/`post-integration-test`), alle Parameter, `mvn condation:run`, Hinweis auf selbst anzulegendes `cms-home`, `.gitignore`-Empfehlung für `cms-home/logs`.
- [ ] **Step 4: Manuell prüfen:** in `modules/example-module` ein `cms-home` (Kopie von `test-server/`) anlegen, `mvn package condation:run`, Server erreichbar, Strg+C beendet ihn, `cms.pid` weg. Ergebnis im Commit-Text festhalten; `cms-home` nicht committen.
- [ ] **Step 5: Commit** `feat: integration test and docs for condation-maven-plugin`.
