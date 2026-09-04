# Collections

Collection detail pages are configured in `config/collections.yaml`. A route can use the item ID
and any number of metadata fields. Nested metadata is addressed with dot notation.

```yaml
collections:
  events:
    detail:
      route: /events/{date:yyyy}/{date:MM}/{date:dd}/{location.country}/{location.city}
      template: collections/event-detail.html
      mappings:
        location.country:
          de: germany
          fr: france
```

A format is appended to its field with `:`, for example `{date:yyyy}`. It uses Java
`DateTimeFormatter` patterns. Date values may be `java.util.Date`, Java time values such as
`LocalDate`, or ISO-8601 strings. A format must not contain `/`. Each desired URL segment therefore
needs its own placeholder; the example produces `/events/2026/09/03/germany/berlin`.

`mappings` translates the exact metadata value before it is converted to a URL slug. Every value
used by an item must have a configured mapping; a missing mapping makes URL generation fail and the
item cannot match an incoming detail route. Routes must resolve to exactly one collection item.

Existing single-field routes remain valid:

```yaml
route: /articles/{id}
route: /articles/{slug}
```
