# CondationCMS updater module

`cms-updater` applies an offline update ZIP to an existing installation. The ZIP contains `manifest.json`, replacement files, and optional migration JARs. The Maven `package` build creates `target/cms-updater-<version>-jar-with-dependencies.jar`; run it with:

```sh
java -jar cms-updater.jar install [--home <installation>] --package <update.zip|URL>
```

The default home is `../`, relative to the current working directory. `--package` accepts a local path, an HTTP(S) URL, or a `file:` URL. The CMS distribution assembly includes the executable as `bin/cms-updater.jar`. Before applying an update, it reads `cms.pid` and validates both the PID and process start time using the same metadata written by the CMS CLI. A matching live process stops the update; stale PID files are removed. The current version is read from `com/condation/cms/application.properties` inside the single `cms-server*.jar` in the home directory, with the JAR filename as a fallback.

```json
{
  "product": "condationcms",
  "version": "8.5.0",
  "minimumVersion": "8.2.0",
  "files": [
    { "source": "files/lib/cms-server.jar", "target": "lib/cms-server.jar", "sha256": "<64 hex characters>" }
  ],
  "migrations": [
    { "id": "8.3-normalize-config", "artifact": "migrations/normalize-config.jar", "sha256": "<64 hex characters>" }
  ]
}
```

Files listed in the manifest are copied to the installation root. Unlisted files, including local configuration and content, are left alone. Existing replaced files are backed up under `data/update-backups/<target-version>/`. The updater writes the installed version to `data/system/cms-version` after all migrations succeed.

Migration JARs provide implementations through Java `ServiceLoader`: include `META-INF/services/com.condation.cms.updater.Migration` with the provider class name. The provider's `id()` must match the manifest entry. The manifest order is execution order. A successful ID is persisted in `data/system/update-history.json` and skipped by later packages. Migration implementations should be idempotent because a process can stop before recording completion.

The installer also exposes a Java API. Signed update metadata and rollback of migration side effects remain follow-up work. SHA-256 checksums detect damaged package contents but do not authenticate who produced a package.

With the `cms-server` Maven `dist` profile, the build creates `cms-server/target/cms-server-<version>-update.zip` alongside the normal distribution ZIP. It lists every server/library file with its checksum, marks `lib/` for full replacement, and removes old versioned `cms-server-*.jar` files before copying the new server JAR. Place optional migration JARs in `cms-server/src/assembly/migrations/`; each JAR's basename is its migration ID. Override `cms.update.minimum.version` to set the oldest supported source version; it defaults to `8.2.0`.
