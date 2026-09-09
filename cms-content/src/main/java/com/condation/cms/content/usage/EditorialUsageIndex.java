package com.condation.cms.content.usage;

/*-
 * #%L
 * CMS Content
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
import com.condation.cms.api.usage.*;
import com.condation.cms.api.ui.elements.ContentTypes;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.content.CollectionRouteTemplate;
import com.condation.cms.core.content.io.ContentFileParser;
import com.google.gson.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Persistent usage index for exactly one site.
 */
public final class EditorialUsageIndex implements UsageIndex, AutoCloseable {

    private final UsageSite site;
    private LuceneUsageStore store;
    private volatile boolean reconciliationPending = true;
    private volatile UsageProblem operationalProblem;

    public EditorialUsageIndex(UsageSite site) {
        this.site = Objects.requireNonNull(site);
        try {
            store = new LuceneUsageStore(site.root());
            store.visitSources(source -> {
                var resource = source.document().resource();
                if (!resource.site().equals(site.id())) {
                    store.delete(resource);
                }
            });
            var siteProblems = store.siteProblems().stream().filter(problem -> problem.site().equals(site.id())).toList();
            store.commit(siteProblems);

        } catch (IOException ex) {
            if (this.store != null) {
                try {
                    store.close();
                } catch (IOException ex1) {
                    throw new UncheckedIOException("Cannot closing usage index for " + site.id(), ex);
                }
            }
            throw new UncheckedIOException("Cannot open usage index for " + site.id(), ex);
        }
    }

    /**
     * Startup/configuration reconciliation: unchanged files and schemas reuse
     * stored extractions.
     */
    public synchronized void synchronize() {
        reconcile(false);
    }

    @Override
    public synchronized void rebuild() {
        reconcile(true);
    }

    /**
     * Called after the primary metadata index has processed a file or directory
     * change.
     */
    public synchronized void refresh(Path changed) {
        Path file = changed.toAbsolutePath().normalize();
        var root = sourceRoot(site, file);
        if (root == null) {
            return;
        }
        try {
            if (!file.toString().endsWith(".md")) {
                updateSite(false);
            } else {
                updateFile(root, file);
            }
            operationalProblem = null;
        } catch (IOException ex) {
            operationalProblem = problem(site, "", "Usage index update failed", ex);
        }
    }

    private void reconcile(boolean force) {
        try {
            updateSite(force);
            reconciliationPending = false;
            operationalProblem = null;
        } catch (IOException ex) {
            operationalProblem = problem(site, "", "Usage index reconciliation failed", ex);
        }
    }

    private void updateSite(boolean force) throws IOException {
        var siteProblems = new ArrayList<UsageProblem>();
        var seen = new HashSet<UsageResource>();
        boolean scanCompleted = false;
        try {
            ContentTypes types = site.contentTypes().get();
            String schema = schema(types);
            for (String folder : List.of("content", "collections")) {
                Path root = site.root().resolve(folder);
                if (!Files.isDirectory(root)) {
                    continue;
                }
                try (var files = Files.walk(root)) {
                    for (var file : files.filter(Files::isRegularFile).filter(PathUtil::isContentFile).sorted().toList()) {
                        var resource = resource(site, root, file);
                        if (!localSource(site, resource)) {
                            continue;
                        }
                        seen.add(resource);
                        var old = store.source(resource);
                        var extractionProblems = new ArrayList<UsageProblem>();
                        try {
                            String stamp = fileStamp(file);
                            if (!force && old.isPresent() && stamp.equals(old.get().fileStamp())
                                    && schema.equals(old.get().schema())) {
                                updateRoute(old.get());
                            } else {
                                store.update(readFile(types, schema, root, file, old, extractionProblems));
                            }
                        } catch (Exception ex) {
                            extractionProblems.add(problem(site, resource.path(), "Extraction failed", ex));
                            retainLastGood(old, extractionProblems, siteProblems);
                        }
                    }
                }
            }
            scanCompleted = true;
        } catch (Exception ex) {
            siteProblems.add(problem(site, "", "Extraction failed", ex));
        }
        if (scanCompleted) {
            store.visitSources(source -> {
                var resource = source.document().resource();
                if (resource.site().equals(site.id()) && !seen.contains(resource)) {
                    store.delete(resource);
                }
            });
        }
        publishCommittedSources(siteProblems);
    }

    private void updateFile(Path root, Path file) throws IOException {
        var resource = resource(site, root, file);
        var siteProblems = new ArrayList<>(store.siteProblems());
        siteProblems.removeIf(problem -> problem.site().equals(site.id()) && problem.path().equals(resource.path()));
        var old = store.source(resource);
        if (!Files.exists(file) || !localSource(site, resource)) {
            store.delete(resource);
        } else {
            var extractionProblems = new ArrayList<UsageProblem>();
            try {
                var types = site.contentTypes().get();
                store.update(readFile(types, schema(types), root, file, old, extractionProblems));
            } catch (Exception ex) {
                extractionProblems.add(problem(site, resource.path(), "Extraction failed", ex));
                retainLastGood(old, extractionProblems, siteProblems);
            }
        }
        publishCommittedSources(siteProblems);
    }

    private PersistedUsageSource readFile(ContentTypes types, String schema, Path root, Path file,
            Optional<PersistedUsageSource> old, List<UsageProblem> extractionProblems) throws IOException {
        if (!file.toRealPath().startsWith(root.toRealPath())) {
            throw new IOException("Source is outside its site root");
        }
        var resource = resource(site, root, file);
        String before = fileStamp(file);
        var parser = new ContentFileParser(file.toString());
        var metadata = parser.getHeader();
        String publicPath = publicPath(site, resource, metadata);
        var references = new UsageExtractor(types, site, extractionProblems).extract(resource, metadata, parser.getContent());
        if (!before.equals(fileStamp(file))) {
            throw new IOException("Source changed during extraction; retry required");
        }
        var document = new UsageDocument(resource, publicPath, metadata, references);
        var previousUsages = old.map(PersistedUsageSource::usages).orElseGet(List::of);
        var problems = List.copyOf(extractionProblems);
        return new PersistedUsageSource(document, before, schema, previousUsages, problems, problems);
    }

    private void updateRoute(PersistedUsageSource source) throws IOException {
        var current = currentDocument(source.document());
        if (!current.equals(source.document())) {
            store.update(new PersistedUsageSource(current, source.fileStamp(), source.schema(), source.usages(),
                    source.extractionProblems(), source.problems()));
        }
    }

    private void retainLastGood(Optional<PersistedUsageSource> old, List<UsageProblem> extractionProblems,
            List<UsageProblem> siteProblems) throws IOException {
        if (old.isEmpty()) {
            siteProblems.addAll(extractionProblems);
            return;
        }
        var source = old.get();
        var document = currentDocument(source.document());
        var problems = List.copyOf(extractionProblems);
        store.update(new PersistedUsageSource(document, source.fileStamp(), source.schema(), source.usages(),
                problems, problems));
    }

    private static boolean localSource(UsageSite site, UsageResource resource) {
        if (resource.kind() != UsageResource.Kind.COLLECTION_ITEM) {
            return true;
        }
        String[] parts = resource.path().split("/");
        return parts.length == 2 && site.collectionSite(parts[0]).equals(site.id());
    }

    private static UsageResource resource(UsageSite site, Path root, Path file) {
        return new UsageResource(site.id(), root.endsWith("collections")
                ? UsageResource.Kind.COLLECTION_ITEM : UsageResource.Kind.CONTENT,
                root.relativize(file).toString().replace('\\', '/'));
    }

    private static String fileStamp(Path file) throws IOException {
        var attributes = Files.readAttributes(file, BasicFileAttributes.class);
        return attributes.lastModifiedTime() + ":" + attributes.size() + ":" + attributes.fileKey();
    }

    private static String schema(ContentTypes types) {
        try {
            // Map iteration order differs between JVMs. Canonicalize object keys before hashing.
            String serialized = canonical(new Gson().toJsonTree(types)).toString();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(("usage-extractor-1\n" + serialized).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static JsonElement canonical(JsonElement element) {
        if (element.isJsonObject()) {
            var result = new JsonObject();
            element.getAsJsonObject().keySet().stream().sorted().forEach(key
                    -> result.add(key, canonical(element.getAsJsonObject().get(key))));
            return result;
        }
        if (element.isJsonArray()) {
            var result = new JsonArray();
            element.getAsJsonArray().forEach(value -> result.add(canonical(value)));
            return result;
        }
        return element;
    }

    private String publicPath(UsageSite site, UsageResource resource, Map<String, Object> metadata) {
        if (resource.kind() == UsageResource.Kind.CONTENT) {
            String path = resource.path();
            String name = Path.of(path).getFileName().toString();
            if (com.condation.cms.api.utils.SectionUtil.isSectionEntry(name)) {
                path = path.substring(0, path.length() - name.length()) + name.substring(0, name.indexOf('.')) + ".md";
            }
            return site.db().getContent().byPath(path).map(node -> node.url()).orElse(PathUtil.toURL(path));
        }
        if (site.collections() == null) {
            return null;
        }
        String[] parts = resource.path().split("/", 2);
        return site.collections().collection(parts[0]).flatMap(definition -> definition.detailPage()).map(detail -> {
            try {
                return new CollectionRouteTemplate(detail).render(parts[1].substring(0, parts[1].length() - 3), metadata);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }).orElse(null);
    }

    private void publishCommittedSources(List<UsageProblem> siteProblems) throws IOException {
        // Publish source facts first so route lookup sees the new route catalog. Edge fields still
        // contain their previous batch until the second commit replaces them atomically per source.
        store.commit(siteProblems);
        var resolver = resolver();
        store.visitSources(source -> {
            var document = source.document();
            var usages = new ArrayList<Usage>();
            var problems = new ArrayList<>(source.extractionProblems());
            var current = currentDocument(document);
            for (var ref : document.references()) {
                try {
                    var target = resolver.resolve(current, ref);
                    if (target.isEmpty()) {
                        continue;
                    }
                    UsageResource resource = target.get();
                    Usage.TargetStatus status = resolver.status(resource);
                    if (resource.kind() == UsageResource.Kind.UNRESOLVED_URL || status == Usage.TargetStatus.MISSING) {
                        var previous = source.usages().stream()
                                .filter(usage -> usage.location().equals(ref.location()) && usage.originalReference().equals(ref.value()))
                                .filter(usage -> usage.target().kind() != UsageResource.Kind.UNRESOLVED_URL).findFirst();
                        if (previous.isPresent()) {
                            resource = previous.get().target();
                            status = Usage.TargetStatus.MISSING;
                        }
                    }
                    usages.add(new Usage(document.resource(), resource, ref.location(), ref.origin(), ref.value(),
                            document.title(), document.status(), status));
                } catch (Exception ex) {
                    problems.add(new UsageProblem(site.id(), document.resource().path(),
                            "Cannot resolve " + ref.location() + ": " + ref.value() + " (" + ex.getMessage() + ")"));
                }
            }
            var next = new PersistedUsageSource(current, source.fileStamp(), source.schema(), List.copyOf(usages),
                    source.extractionProblems(), List.copyOf(problems));
            if (!next.equals(source)) {
                store.update(next);
            }
        });
        store.commit(siteProblems);
    }

    private UsageDocument currentDocument(UsageDocument document) {
        return new UsageDocument(document.resource(), publicPath(site, document.resource(), document.metadata()),
                document.metadata(), document.references());
    }

    private UsageReferenceResolver resolver() {
        return new UsageReferenceResolver(site, new UsageReferenceResolver.PublicTargetLookup() {
            @Override
            public Optional<UsageResource> aliasTarget(String path) throws IOException {
                return store.aliasTarget(path);
            }

            @Override
            public Optional<UsageResource> collectionTarget(String path) throws IOException {
                return store.collectionTarget(path);
            }
        });
    }

    private static Path sourceRoot(UsageSite site, Path file) {
        for (String folder : List.of("content", "collections")) {
            Path root = site.root().resolve(folder);
            if (file.startsWith(root)) {
                return root;
            }
        }
        return null;
    }

    private static UsageProblem problem(UsageSite site, String path, String message, Exception ex) {
        return new UsageProblem(site.id(), path, message + ": " + ex.getMessage());
    }

    @Override
    public List<Usage> incoming(UsageResource target) {
        try {
            return withLiveMediaStatus(store.incoming(target));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public List<Usage> outgoing(UsageResource source) {
        if (!source.site().equals(site.id())) {
            return List.of();
        }
        try {
            return withLiveMediaStatus(store.outgoing(source));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private List<Usage> withLiveMediaStatus(List<Usage> usages) {
        var resolver = resolver();
        return usages.stream().map(usage -> usage.target().kind() != UsageResource.Kind.MEDIA ? usage
                : new Usage(usage.source(), usage.target(), usage.location(), usage.origin(), usage.originalReference(),
                        usage.sourceTitle(), usage.sourceStatus(), resolver.status(usage.target()))).toList();
    }

    @Override
    public synchronized List<UsageProblem> problems() {
        try {
            var problems = new ArrayList<>(store.problems());
            if (reconciliationPending) {
                problems.add(new UsageProblem(site.id(), "", "Startup reconciliation pending"));
            }
            if (operationalProblem != null) {
                problems.add(operationalProblem);
            }
            return problems.stream().distinct().toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public synchronized void close() {
        try {
            store.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
