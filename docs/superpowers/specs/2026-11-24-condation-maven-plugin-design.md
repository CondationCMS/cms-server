# condation-maven-plugin – Design

## Intent
Modul-Entwickler sollen ihr CondationCMS-Modul gegen einen echten CMS-Server testen können, ohne die Modul-Zip von Hand in den `modules`-Ordner eines Server-Homes zu kopieren.

## Goals
- Goals `condation:start`, `condation:stop`, `condation:run`.
- Modul-Zip (`target/<finalName>-bin.zip`) wird beim Start automatisch nach `<home>/modules/<id>/` installiert.
- Nutzbar interaktiv (`mvn condation:run`, Strg+C, Log in der Konsole) und im Build (`pre-/post-integration-test`).

## Non-goals
- Das Plugin legt kein Server-Home an und liefert keine Demo-Site/Themes mit. Der Entwickler pflegt `cms-home` selbst (Sites, Themes, `server.toml`).
- Kein Bauen des Moduls (macht das Assembly-Plugin im Modul-Projekt).

## Constraints
- `server start` ruft `System.exit` auf und liest `cms.home` als System-Property, daher läuft der Server nicht im Maven-Prozess.
- Neues Reactor-Modul `condation-maven-plugin`, Packaging `maven-plugin`, Version wie Parent (8.4.0), wird mit dem normalen Release gebaut.

## Approaches
- A: Server im Maven-Prozess (wie JUnit-Extension) – verworfen: System.exit, Classloader/Polyglot, kein getrenntes start/stop.
- B: Separater Java-Prozess mit aufgelöstem Classpath – **gewählt**.
- C: Distribution entpacken + Start-Script – verworfen: braucht publizierte Distribution, plattformabhängig, kein Vorteil.

## Design (B)
- Classpath: `cms-server` (Version `cmsVersion`, Default Plugin-Version) wird per Maven-Resolver samt Abhängigkeiten aufgelöst.
- Start: `java -Dcms.home=<home> -cp <cp> com.condation.cms.cli.CMSCli server start`, Arbeitsverzeichnis = Home, Ausgabe nach `<home>/logs/plugin-server.out` (bei `run`: in die Konsole).
- `start`: Home prüfen (fehlt → klarer Fehler), Modul installieren, Prozess starten, warten bis PID-File vorhanden und HTTP-Port aus `server.toml` antwortet (`startTimeout`, Default 60s), dann zurückkehren; Server läuft weiter.
- `stop`: `CMSCli server stop` mit gleichem Home; Fallback: Prozess aus `cms.pid` beenden.
- `run`: wie `start`, aber im Vordergrund; Strg+C (Shutdown-Hook) stoppt den Server.
- Parameter: `cmsHome` (Default `${project.basedir}/cms-home`), `cmsVersion`, `moduleZip` (Default `target/${project.build.finalName}-bin.zip` bzw. Artifact mit Classifier `bin`), `installModule` (Default true), `startTimeout`, `jvmArgs`, `skip`.
- Modul-Installation: Zip entpacken, alten Ordner `<home>/modules/<id>/` vorher löschen; Zip-Struktur `<id>/{module.properties,libs/}`.

## Left to the builder
- Details der Port-Ermittlung (TOML lesen), Fallback-Mechanik bei `stop`, Integrationstest (maven-plugin-testing / invoker) mit Beispielmodul `modules/example-module`.
- Dokumentation unter `documentation/`.
