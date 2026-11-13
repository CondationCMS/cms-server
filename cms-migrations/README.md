# CondationCMS migrations

This Maven aggregator contains versioned, independently packaged CMS migrations. The migration JAR API is provided by `cms-updater`. Each migration JAR has its own `Migration` provider and a stable ID matching its final JAR filename without `.jar`.

The first migration, `migration-8.4.0-to-8.5.0`, is built in the reactor and staged into the update package only when the `cms-server` version is `8.5.0`.
