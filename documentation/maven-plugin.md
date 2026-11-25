### Maven plugin for module development

The `condation-maven-plugin` starts and stops a CondationCMS server from the build of your module.
It installs the module zip (`*-bin.zip`, built by the assembly plugin) into the `modules` folder of the server home and starts the server as a separate Java process.

The plugin has the same version as the CMS release (`cmsVersion` defaults to the plugin version).

New modules created with the `cms-module-archetype` already contain the plugin and a minimal `cms-home`:
`mvn package condation:run` works right away (see `cms-module-archetype/README.md`).

#### Prepare a server home

The plugin does **not** create a home for you. Create a directory (default: `cms-home` in your project) with at least:

```
cms-home/
  server.toml     # [server] port, [ipc], [ui] secret
  hosts/          # your test sites
  themes/         # themes required by your sites
```

Add `cms-home/logs`, `cms-home/modules`, `cms-home/cms.pid` to your `.gitignore`.

#### Goals

| Goal | Description |
|------|-------------|
| `condation:start` | installs the module, starts the server in the background and waits until `cms.pid` exists and the port from `server.toml` accepts connections |
| `condation:stop` | stops the server (`server stop`, falls back to terminating the process) |
| `condation:run` | installs the module and runs the server in the foreground, log output in the console, `Ctrl+C` stops it |

```
mvn package condation:run
```

The server log of `start` is written to `<home>/logs/plugin-server.out`.

#### Add an extension: `condation:add`

```
mvn condation:add
```

asks for the type of the extension (a list: move with arrow up/down, confirm with enter, ctrl+c aborts), the class name, the package and the
type specific values (e.g. the shortcode name or the API path) and creates the class in `src/main/java`. The class
already contains a first working element, e.g. a `@ShortCode("hello")` method. The package defaults to the package of
your existing `@Extension` classes. An existing file is only replaced after confirmation. On terminals without cursor key support (`TERM=dumb`) the list is a
numbered text prompt instead.

Types: `hook` (action/filter), `shortcode`, `template-function`, `template-component`, `realtime` (SSE/WebSocket),
`api-handler`, `http-handler`, `http-routes`, `routes`, `template-model`, `query-operator`, `variant-selector`,
`backup-post-processing`, `cache-provider`, `markdown-renderer`, `template-engine`, `server-lifecycle`,
`server-hook` and `module-lifecycle` (activate/deactivate of the module).

Without a terminal (CI, scripts) every value is given as property:

```
mvn condation:add -Dcondation.add.type=shortcode -Dcondation.add.name=HelloTag \
    -Dcondation.add.package=com.example.mod -Dcondation.add.options=name=hello,param=name
```

| Property | Description |
|---|---|
| `condation.add.type` | type id, see above |
| `condation.add.name` | class name |
| `condation.add.package` | package of the class |
| `condation.add.options` | answers of the type specific questions as `key=value,key=value` (shortcode: `name`, `param`; hook: `kind`, `hook`, `scope`; realtime: `transport`, `path`; api-handler/routes: `path`, `method` ...) |
| `condation.add.overwrite` | replace an existing file without asking |
| `condation.add.skip` | skip `add` (independent of `condation.skip`, which only switches off `start`, `stop` and `run`) |

#### Use in integration tests

```xml
<plugin>
    <groupId>com.condation.cms</groupId>
    <artifactId>condation-maven-plugin</artifactId>
    <version>${cms.version}</version>
    <executions>
        <execution>
            <id>start-cms</id>
            <goals><goal>start</goal></goals>
        </execution>
        <execution>
            <id>stop-cms</id>
            <goals><goal>stop</goal></goals>
        </execution>
    </executions>
</plugin>
```

`start` is bound to `pre-integration-test` and `stop` to `post-integration-test` by default.

#### Parameters

| Parameter / property | Default | Description |
|---|---|---|
| `cmsHome` / `condation.home` | `${project.basedir}/cms-home` | server home, must exist |
| `cmsVersion` / `condation.version` | plugin version | version of `cms-server` to run |
| `moduleZip` / `condation.moduleZip` | auto | module zip; otherwise the attached `bin` zip of the build, otherwise the single `target/*-bin.zip` |
| `installModule` / `condation.installModule` | `true` | install the module zip before start |
| `startTimeout` / `condation.startTimeout` | `60` | seconds to wait for the server (and for stop) |
| `jvmArgs` / `condation.jvmArgs` | – | additional JVM arguments for the server |
| `skip` / `condation.skip` | `false` | skip the goal |

The previous module folder `<home>/modules/<id>` is removed before the new one is extracted.

#### Compatibility

- Maven 3.10 and Maven 4.0.0-rc-7 were tested interactively (arrow key selection, batch mode, files created) on macOS with
  JDK 25. The workflow `.github/workflows/maven-plugin-compat.yml` builds and tests the plugin and the archetype on Linux,
  macOS and Windows with Maven 3.9 and Maven 4.
- `condation:add` only prompts if stdin and stdout are a terminal and Maven is not in batch mode (`-B`). Otherwise (CI,
  Git Bash/mintty on Windows, mvnd, redirected output) pass the values as `-Dcondation.add.*` properties; a missing value
  fails with a message naming the property.
- On Unix the terminal uses `stty` and needs no native access. On Windows JLine uses native providers; JDK 24+ prints a
  warning for them unless Maven runs with `--enable-native-access=ALL-UNNAMED`. Projects created from the archetype
  contain `.mvn/jvm.config` with this flag; add it to your own projects the same way.
- Projects created with an older archetype: `condation:add` has its own switch `condation.add.skip` (`<addSkip>`). Add
  `<addSkip>true</addSkip>` next to `<skip>true</skip>` in the parent pom and `<addSkip>false</addSkip>` in the module
  project, otherwise `mvn condation:add` from the project root also asks in the api project.
