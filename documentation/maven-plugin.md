### Maven plugin for module development

The `condation-maven-plugin` starts and stops a CondationCMS server from the build of your module.
It installs the module zip (`*-bin.zip`, built by the assembly plugin) into the `modules` folder of the server home and starts the server as a separate Java process.

The plugin has the same version as the CMS release (`cmsVersion` defaults to the plugin version).

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
