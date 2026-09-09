# Editorial usage index

Each site injector owns one persistent Lucene index of direct references in that site's
pages, sections, variants and local collection items. Drafts and scheduled content are included.
Themes, templates, dynamic queries and shortcode execution are outside its scope.

## Identity and extraction

Resources use `(site, kind, path)` as their identity. Paths are relative to the content,
assets or collections root, including the filename; collection paths include the collection:

```text
(de, CONTENT, about/index.md)
(de, MEDIA, images/logo.svg)
(shared, COLLECTION_ITEM, authors/jane.md)
```

The existing `manager/contentTypes/register` hook supplies schemas through the shared
`ContentTypeProvider`. Background indexing loads these in a temporary execution context
using `RequestContextFactory.contentTypes()`; the manager need not be open.

Media, reference and collection fields are inspected, including tabs, dot-separated field
names and nested lists. List forms use the enclosing type's named form, then the globally
registered list item type, following the manager's convention. List item forms are not
also interpreted as top-level metadata forms. Missing or ambiguous schemas produce a
coverage problem; body references can still be indexed.

Markdown extraction uses the CMS inline tokenizer for inline links, images and linked
images. Raw HTML supports `href`, `src`, `poster` and `srcset`. Code fences, code spans,
indented code, HTML comments and script/style/pre/code contents are excluded. Locations
use a metadata field path or a text-node/HTML-element location, not a source-file line number.
Markdown constructs unsupported by the CMS tokenizer and references in arbitrary string,
code or shortcode parameters are not inferred.

## Paths and site contexts

Typed field values are site-local paths. Their context prefix is **not** removed. Reference
fields can specify a target site; collection fields resolve the configured collection's
owning site. Existing content URLs accepted in reference fields are resolved to file paths.

Raw HTML URLs and Markdown image URLs are public URLs. Relative URLs are resolved against
the source page's public URL (sections use their owning page). Host, port and the current
site's context must match before that context is stripped. `/de` never matches `/details`.
An absolute HTTP(S) URL or a protocol-relative URL is internal only if it matches the site.

Markdown **links** already receive the site context through `HTTPUtil.prependContext`
when the CMS renders them. The index reproduces that behavior before resolving the URL.
For a German site with context `/de`, `[About](/about)` therefore refers to its own
`/about`, while raw HTML `<a href="/about">` is outside this site and remains unresolved.
No templates are evaluated to discover `cms.links.createUrl` calls.

Media URLs under `/assets/` and `/media/`, including format/preview query parameters,
resolve to the original asset. Fragment-only links and non-HTTP schemes are ignored.
Content custom URLs, aliases and configured collection detail routes resolve to file paths.
Collection routes are calculated from raw metadata and indexed as exact route fields, so
unpublished targets are included without loading all collection items into memory.
Relative URLs in collection items without a configured detail route remain unresolved;
their eventual embedding page cannot be inferred statically.

## Updates and incomplete results

Each site's outgoing references are stored under `data/usage/index`. Incoming and outgoing
queries run only against that site's Lucene index. A target may carry another site ID, for
example for an explicitly targeted reference or shared collection, but the index never opens
or aggregates another site's store. Such a target's existence status remains `UNRESOLVED`
inside this index.
One Lucene document per source stores its extracted references, resolved usages, last known
targets, metadata and coverage problems. Inverted target, alias and collection-route fields
support reverse and public-route lookup.
Updates to both reference directions become visible together after a durable commit.

When the site-scoped instance is created, stored usages are immediately queryable directly
from Lucene. No complete in-memory copy is restored. When its host becomes ready, the index
compares filesystem stamps (modification time, size and file key) and a canonical fingerprint
of the Content Types. Only new/changed files or changed schemas require extraction. Deleted
sources are removed. This detects changes made while the server was stopped; deliberately
preserving all file-stamp attributes while changing bytes requires an explicit rebuild.
Stored sources are streamed from a Lucene reader snapshot and their references are resolved
again against the current indexed route catalog. Unchanged Lucene documents are not rewritten.
The index format and extraction algorithm are versioned.

Content changes are processed after the
content metadata index; `CollectionChangedEvent` likewise runs after collection metadata
updates, including manager saves and filesystem changes. Updating a source replaces its
outgoing references atomically. Directory changes rescan the source site. Other stored
references are resolved again because target routes may have changed, without rereading
their bodies. Media existence is checked when usages are requested.

Configuration/host reloads and `ContentTypesChangedEvent` reconcile the index. Changes in a
site's `extensions` directory publish the latter event; extensions changing schemas by
other means should publish it explicitly. `UsageIndex.rebuild()` forces a complete extraction
when needed; the existing host reindex command also forces extraction for that site.
Shutdown commits and closes the Lucene writers without deleting their data.

Deleting a source removes its outgoing references. Incoming references to deleted targets
remain. Unresolvable public URLs use kind `UNRESOLVED_URL`. When a previously resolved URL
breaks, the index retains its last known target with status `MISSING`, including after a
restart. A URL that was already broken before the first extraction remains unresolved;
its original target cannot be inferred. Removing the index data also removes this history.
Renaming is treated as deletion plus creation. References are not rewritten automatically.

Parse/read failures retain the last successfully extracted references and publish a coverage
problem. Before initial indexing finishes, a pending problem is reported. An empty result
must be labelled **“No usages found in editorial content”**, never proof that deletion is safe.

## Java and manager API

`UsageIndex` is a site-scoped singleton registered by `SiteModule` and available through the
site injector:

```java
var target = new UsageResource("de", UsageResource.Kind.MEDIA, "images/logo.svg");
var incoming = usageIndex.incoming(target);
var outgoing = usageIndex.outgoing(
    new UsageResource("de", UsageResource.Kind.CONTENT, "index.md"));
var incompleteSources = usageIndex.problems();
```

Manager RPC methods require `content.edit`:

| Method | Parameters | Result |
| --- | --- | --- |
| `usage.incoming` | `kind`, `path` | `{items, problems, scope: "editorial"}`; includes the current source site's coverage problems |
| `usage.outgoing` | `kind`, `path` | `{items, problems, scope: "editorial"}`; current site's coverage problems |
| `usage.problems` | none | Current site's coverage problems |

The requested resource belongs to the current manager site; shared collection items are
resolved to their configured source site. Incoming results contain only sources managed by
the current site's index. Each usage includes source/target identities, location, origin,
original reference, source title/status and target status (`EXISTS`, `MISSING`, `UNRESOLVED`).
The manager shortcut **Show usages** in the Page section (**Ctrl+9**) opens a read-only
dialog for the current preview node, including variants and collection detail pages.
It lists incoming references with source title/path, site, location, original reference and
source status. Empty results are described as no indexed usages; coverage problems are
shown explicitly. Themes, templates and dynamic queries remain outside the scope.
The dialog uses the active preview identity or resolves the preview URL through `content.node`,
so public site contexts are not mistaken for stored file paths.
