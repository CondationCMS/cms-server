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
import com.condation.cms.api.utils.PathUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Persistent inverted index for one source site. All reference directions share a commit. */
final class LuceneUsageStore implements AutoCloseable {
    private static final String VERSION = "2";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String ALIAS = "alias";
    private static final String COLLECTION_ROUTE = "collection-route";
    private static final Gson JSON = new Gson();
    private final FSDirectory directory;
    private final IndexWriter writer;
    private final SearcherManager searchers;
    private boolean dirty;

    LuceneUsageStore(Path siteRoot) throws IOException {
        directory = FSDirectory.open(siteRoot.resolve("data/usage/index"));
        IndexWriter openedWriter = null;
        SearcherManager openedSearchers = null;
        try {
            boolean recreate = false;
            if (DirectoryReader.indexExists(directory)) {
                try (var existing = DirectoryReader.open(directory)) {
                    recreate = !VERSION.equals(existing.getIndexCommit().getUserData().get("usage-format"));
                }
            }
            openedWriter = new IndexWriter(directory, new IndexWriterConfig(new KeywordAnalyzer())
                    .setOpenMode(recreate ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND));
            writer = openedWriter;
            openedSearchers = new SearcherManager(writer, true, true, new SearcherFactory());
            searchers = openedSearchers;
            var settings = new HashMap<String, String>();
            writer.getLiveCommitData().forEach(entry -> settings.put(entry.getKey(), entry.getValue()));
            dirty = !VERSION.equals(settings.get("usage-format"));
        } catch (Exception ex) {
            if (openedSearchers != null) openedSearchers.close();
            if (openedWriter != null) openedWriter.close();
            directory.close();
            throw ex;
        }
    }

    synchronized List<PersistedUsageSource> sources() throws IOException {
        var result = new ArrayList<PersistedUsageSource>();
        visitSources(result::add);
        return List.copyOf(result);
    }

    synchronized Optional<PersistedUsageSource> source(UsageResource resource) throws IOException {
        var documents = search(new TermQuery(new Term(SOURCE, key(resource))), 1);
        return documents.isEmpty() ? Optional.empty() : Optional.of(source(documents.getFirst()));
    }

    synchronized void visitSources(SourceVisitor visitor) throws IOException {
        var searcher = searchers.acquire();
        try {
            var stored = searcher.storedFields();
            ScoreDoc after = null;
            while (true) {
                var page = searcher.searchAfter(after, MatchAllDocsQuery.INSTANCE, 256);
                for (var hit : page.scoreDocs) visitor.accept(source(stored.document(hit.doc)));
                if (page.scoreDocs.length < 256) return;
                after = page.scoreDocs[page.scoreDocs.length - 1];
            }
        } finally {
            searchers.release(searcher);
        }
    }

    synchronized void update(PersistedUsageSource source) throws IOException {
        var value = source.document();
        var document = new Document();
        document.add(new StringField(SOURCE, key(value.resource()), Field.Store.NO));
        source.usages().stream().map(Usage::target).distinct().forEach(target ->
                document.add(new StringField(TARGET, key(target), Field.Store.NO)));
        if (value.resource().kind() == UsageResource.Kind.CONTENT
                && value.metadata().get("aliases") instanceof Collection<?> aliases) {
            aliases.stream().filter(String.class::isInstance).map(String.class::cast)
                    .map(PathUtil::normalizeURL).distinct().forEach(alias ->
                        document.add(new StringField(ALIAS, alias, Field.Store.NO)));
        }
        if (value.resource().kind() == UsageResource.Kind.COLLECTION_ITEM && value.publicPath() != null) {
            document.add(new StringField(COLLECTION_ROUTE, PathUtil.normalizeURL(value.publicPath()), Field.Store.NO));
        }
        document.add(new StoredField("resource", JSON.toJson(value.resource())));
        if (value.publicPath() != null) document.add(new StoredField("public-path", value.publicPath()));
        // YAML preserves date/number metadata types required by collection route templates.
        document.add(new StoredField("metadata", new Yaml().dump(value.metadata())));
        document.add(new StoredField("references", JSON.toJson(value.references())));
        document.add(new StoredField("usages", JSON.toJson(source.usages())));
        document.add(new StoredField("problems", JSON.toJson(source.problems())));
        document.add(new StoredField("extraction-problems", JSON.toJson(source.extractionProblems())));
        document.add(new StoredField("file-stamp", source.fileStamp()));
        document.add(new StoredField("schema", source.schema()));
        writer.updateDocument(new Term(SOURCE, key(value.resource())), document);
        dirty = true;
    }

    synchronized void delete(UsageResource source) throws IOException {
        writer.deleteDocuments(new Term(SOURCE, key(source)));
        dirty = true;
    }

    List<Usage> incoming(UsageResource target) throws IOException {
        return search(new TermQuery(new Term(TARGET, key(target)))).stream().flatMap(doc -> usages(doc).stream())
                .filter(usage -> usage.target().equals(target)).toList();
    }

    List<Usage> outgoing(UsageResource source) throws IOException {
        return search(new TermQuery(new Term(SOURCE, key(source)))).stream().flatMap(doc -> usages(doc).stream()).toList();
    }

    synchronized Optional<UsageResource> aliasTarget(String path) throws IOException {
        return uniqueTarget(ALIAS, path);
    }

    synchronized Optional<UsageResource> collectionTarget(String path) throws IOException {
        return uniqueTarget(COLLECTION_ROUTE, path);
    }

    synchronized List<UsageProblem> siteProblems() {
        var settings = new HashMap<String, String>();
        writer.getLiveCommitData().forEach(entry -> settings.put(entry.getKey(), entry.getValue()));
        String problems = settings.get("site-problems");
        return problems == null ? List.of()
                : JSON.fromJson(problems, new TypeToken<List<UsageProblem>>() {}.getType());
    }

    synchronized List<UsageProblem> problems() throws IOException {
        var result = new ArrayList<>(siteProblems());
        visitSources(source -> result.addAll(source.problems()));
        return result.stream().distinct().toList();
    }

    synchronized void commit(List<UsageProblem> problems) throws IOException {
        var nextProblems = List.copyOf(problems);
        if (!siteProblems().equals(nextProblems)) dirty = true;
        if (!dirty) return;
        writer.setLiveCommitData(Map.of("usage-format", VERSION, "site-problems", JSON.toJson(nextProblems)).entrySet());
        writer.commit();
        searchers.maybeRefreshBlocking();
        dirty = false;
    }

    private List<Document> search(Query query) throws IOException {
        return search(query, Integer.MAX_VALUE);
    }

    private List<Document> search(Query query, int limit) throws IOException {
        var searcher = searchers.acquire();
        try {
            var stored = searcher.storedFields();
            var result = new ArrayList<Document>();
            ScoreDoc after = null;
            while (result.size() < limit) {
                int pageSize = Math.min(256, limit - result.size());
                var page = searcher.searchAfter(after, query, pageSize);
                for (var hit : page.scoreDocs) result.add(stored.document(hit.doc));
                if (page.scoreDocs.length < pageSize) break;
                after = page.scoreDocs[page.scoreDocs.length - 1];
            }
            return result;
        } finally {
            searchers.release(searcher);
        }
    }

    private Optional<UsageResource> uniqueTarget(String field, String path) throws IOException {
        var documents = search(new TermQuery(new Term(field, PathUtil.normalizeURL(path))), 2);
        return documents.size() == 1
                ? Optional.of(JSON.fromJson(documents.getFirst().get("resource"), UsageResource.class))
                : Optional.empty();
    }

    private static PersistedUsageSource source(Document document) {
        UsageResource resource = JSON.fromJson(document.get("resource"), UsageResource.class);
        Map<String, Object> metadata = new Yaml(new SafeConstructor(new LoaderOptions())).load(document.get("metadata"));
        List<UsageReference> references = JSON.fromJson(document.get("references"), new TypeToken<List<UsageReference>>() {}.getType());
        List<UsageProblem> problems = JSON.fromJson(document.get("problems"), new TypeToken<List<UsageProblem>>() {}.getType());
        List<UsageProblem> extractionProblems = JSON.fromJson(document.get("extraction-problems"), new TypeToken<List<UsageProblem>>() {}.getType());
        return new PersistedUsageSource(new UsageDocument(resource, document.get("public-path"), metadata, references),
                document.get("file-stamp"), document.get("schema"), usages(document), extractionProblems, problems);
    }

    private static List<Usage> usages(Document document) {
        return JSON.fromJson(document.get("usages"), new TypeToken<List<Usage>>() {}.getType());
    }

    private static String key(UsageResource resource) { return JSON.toJson(resource); }

    @FunctionalInterface
    interface SourceVisitor {
        void accept(PersistedUsageSource source) throws IOException;
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            commit(siteProblems());
        } finally {
            try { searchers.close(); }
            finally {
                try { writer.close(); }
                finally { directory.close(); }
            }
        }
    }
}
