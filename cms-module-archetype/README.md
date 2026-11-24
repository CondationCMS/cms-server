# CondationCMS Module Archetype

Creates a Maven multi-module project for a CondationCMS module (modelled on `example-module`):
`<artifactId>-parent` (pom), `<artifactId>-api` and `<artifactId>` (the module, built as a distributable zip).

The archetype is part of the CMS build and has the **same version as the CMS**. The generated project uses
the CMS, `condation-maven-plugin`, modules-framework and Lombok versions of that release.

## Generate a module

```shell
mvn org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate \
  -DarchetypeGroupId=com.condation.cms.modules \
  -DarchetypeArtifactId=cms-module-archetype \
  -DarchetypeVersion=8.4.0 \
  -DgroupId=com.example.cms.modules \
  -DartifactId=my-module \
  -Dversion=1.0.0 \
  -Dpackage=com.example.cms.modules.mymodule \
  -DmoduleName="My module" \
  -DinteractiveMode=false
```

Before the archetype is published, run `mvn install -pl cms-module-archetype -am` in the CMS repository and add
`-DarchetypeCatalog=local`. Run the command in the empty target directory (PowerShell: pass the arguments as an array).

`moduleName` defaults to the artifact id. The module's artifact id and module id are `artifactId`, the API artifact is
`artifactId-api`, the parent is `artifactId-parent`.

## Build and run the module

```shell
cd my-module
mvn package                  # module zip: module/target/my-module-bin.zip
mvn package condation:run    # installs the zip into module/cms-home and runs the CMS (Ctrl+C stops it)
```

The generated project contains the `condation-maven-plugin` and a minimal server home `module/cms-home`
(`server.toml` with development values, the example site in `hosts/local`, an empty `themes/`). Add your test sites and themes there.
See `documentation/maven-plugin.md` for all goals and parameters.

The generated module contains two example extensions: `ModuleHooks` (registers a hook) and `ModuleLifeCycle`
(`activate()`/`deactivate()`, logs when the module is activated).

`module/cms-home/hosts/local` is a minimal example site (`site.toml`, `templates/index.html`, `content/index.md`)
that is served at `http://localhost:2020/`. The module is activated for the site under `[modules] active` in its
`site.toml`; rename or add sites as you like.

Recommended `.gitignore` entries of the generated project (the archetype cannot ship the file itself):

```
target/
module/cms-home/logs/
module/cms-home/modules/
module/cms-home/cms.pid
module/cms-home/temp/
```

## Tests

`mvn verify -pl cms-module-archetype` generates a project from the archetype (`src/test/resources/projects/basic`)
and runs `mvn verify` on it. This needs `cms-api` and `condation-maven-plugin` of the same version in the local
repository (`mvn install` of the CMS build).
