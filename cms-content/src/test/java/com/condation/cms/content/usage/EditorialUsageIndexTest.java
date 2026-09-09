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

import com.condation.cms.api.SiteProperties;
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.*;
import com.condation.cms.api.eventbus.events.*;
import com.condation.cms.api.ui.elements.*;
import com.condation.cms.api.ui.elements.fields.*;
import com.condation.cms.api.usage.*;
import com.condation.cms.core.content.io.ContentFileParser;
import com.condation.cms.core.eventbus.DefaultEventBus;
import com.condation.cms.core.serivce.ServiceRegistry;
import com.condation.cms.core.serivce.impl.SiteDBService;
import com.condation.cms.filesystem.FileDB;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

class EditorialUsageIndexTest {
    @TempDir Path temp;
    EditorialUsageIndex index;
    final List<EditorialUsageIndex> indexes = new ArrayList<>();
    final List<FileDB> databases = new ArrayList<>();
    final Map<String, DefaultEventBus> buses = new HashMap<>();

    @AfterEach void close() throws Exception {
        for (var usageIndex : indexes) usageIndex.close();
        for (var db : databases) db.close();
        ServiceRegistry.getInstance().clear();
    }

    @Test void extractsTypedNestedAndBodyReferencesWithoutRenderingTemplates() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"), new ReferenceField("related", "Related"),
                new CollectionField("author", "Author", "authors"), new ListField("teasers", "Teasers"),
                new MarkdownField("description", "Description"));
        types.registerListItemType(new ListItemType("teasers", new FormDefinition(List.of(new MediaField("image", "Image")))));
        types.registerCollection(new CollectionType("authors", Map.of("main", new FormDefinition(List.of(new MediaField("portrait", "Portrait"))))));
        write("en/content/index.md", "template: page.html\nstatus: draft\nhero: images/hero.svg\nrelated: about.md\nauthor: jane\nteasers:\n  - image: images/teaser.svg\ndescription: '[About](/about)'",
                "![Hero](/media/images/hero.svg?format=small)\n<a href='/about#team'>Team</a>\n<img srcset='/assets/images/one.svg 1x, /assets/images/two.svg 2x'>\n"
                + "```html\n<img src='/media/ignored.svg'>\n```\n`[ignored](/ignored)`\n[external](https://elsewhere.test/a)\n[mail](mailto:a@example.org)");
        write("en/content/about.md", "template: page.html", "About");
        write("en/collections/authors/jane.md", "portrait: images/jane.svg", "Jane");
        write("en/templates/page.html", "", "<img src='/media/template-only.svg'>");
        Files.createDirectories(temp.resolve("en/assets/images"));
        Files.writeString(temp.resolve("en/assets/images/hero.svg"), "svg");
        site("en", "/", types, Map.of());
        index.rebuild();

        var usages = index.outgoing(key("en", UsageResource.Kind.CONTENT, "index.md"));
        assertThat(usages).extracting(Usage::target).contains(
                key("en", UsageResource.Kind.MEDIA, "images/hero.svg"),
                key("en", UsageResource.Kind.MEDIA, "images/teaser.svg"),
                key("en", UsageResource.Kind.CONTENT, "about.md"),
                key("en", UsageResource.Kind.COLLECTION_ITEM, "authors/jane.md"));
        assertThat(usages).allMatch(usage -> usage.sourceStatus().equals("draft"));
        assertThat(usages).noneMatch(usage -> usage.originalReference().contains("ignored") || usage.originalReference().contains("elsewhere") || usage.originalReference().startsWith("mailto:"));
        assertThat(usages).anyMatch(usage -> usage.location().equals("metadata.teasers[0].image"));
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "images/hero.svg"))).hasSize(2)
                .allMatch(usage -> usage.targetStatus() == Usage.TargetStatus.EXISTS);
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "images/jane.svg"))).singleElement()
                .satisfies(usage -> assertThat(usage.source()).isEqualTo(key("en", UsageResource.Kind.COLLECTION_ITEM, "authors/jane.md")));
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "template-only.svg"))).isEmpty();
        assertThat(index.problems()).isEmpty();
    }

    @Test void resolvesOnlyTheCurrentSitesPublicContextAndKeepsExplicitTargetSites() throws Exception {
        var types = pageTypes(new ReferenceField("related", "Related"), new ReferenceField("english", "English", "en"));
        write("en/content/about.md", "template: page.html", "");
        write("en/content/details.md", "template: page.html", "");
        write("de/content/about.md", "template: page.html", "");
        write("de/content/de/nested.md", "template: page.html", "");
        write("de/content/index.md", "template: page.html\nrelated: de/nested.md\nenglish: about.md",
                "[German](/about)\n<a href='/about'>English</a><a href='/de/about'>German</a>"
                + "<a href='https://example.test/de/about?preview=manager#x'>German absolute</a>"
                + "<a href='/details'>English details</a><a href='//elsewhere.test/de/about'>External</a>");
        site("en", "/", types, Map.of());
        site("de", "/de", types, Map.of());
        index.rebuild();

        var usages = index.outgoing(key("de", UsageResource.Kind.CONTENT, "index.md"));
        assertThat(usages).filteredOn(usage -> usage.origin() == Usage.Origin.MARKDOWN).singleElement()
                .satisfies(usage -> assertThat(usage.target()).isEqualTo(key("de", UsageResource.Kind.CONTENT, "about.md")));
        assertThat(usages).filteredOn(usage -> usage.location().equals("metadata.related")).singleElement()
                .satisfies(usage -> assertThat(usage.target().path()).isEqualTo("de/nested.md"));
        assertThat(usages).filteredOn(usage -> usage.originalReference().equals("/about") && usage.origin() == Usage.Origin.HTML).singleElement()
                .satisfies(usage -> assertThat(usage.target().kind()).isEqualTo(UsageResource.Kind.UNRESOLVED_URL));
        assertThat(usages).anyMatch(usage -> usage.location().equals("metadata.english")
                && usage.target().equals(key("en", UsageResource.Kind.CONTENT, "about.md")));
        assertThat(usages).noneMatch(usage -> usage.target().equals(key("en", UsageResource.Kind.CONTENT, "details.md")));
        assertThat(usages).noneMatch(usage -> usage.originalReference().startsWith("//elsewhere"));
        assertThat(index.problems()).isEmpty();
    }

    @Test void replacesOutgoingEdgesAndKeepsBrokenIncomingEdgesAcrossDeletion() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        var source = write("en/content/index.md", "template: page.html\nhero: first.svg", "[About](/about)");
        var about = write("en/content/about.md", "template: page.html", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        write("en/content/index.md", "template: page.html\nhero: second.svg", "[About](/about)");
        index.refresh(source);
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "first.svg"))).isEmpty();
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).hasSize(1);

        Files.delete(about);
        ((FileDB) site.db()).reindex();
        index.refresh(about);
        assertThat(index.incoming(key("en", UsageResource.Kind.CONTENT, "about.md"))).singleElement()
                .satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.MISSING));
        Files.delete(source);
        index.refresh(source);
        assertThat(index.incoming(key("en", UsageResource.Kind.CONTENT, "about.md"))).isEmpty();
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).isEmpty();
    }

    @Test void resolvesCollectionRoutesAndSharedCollectionsToTheirOwningSite() throws Exception {
        var types = pageTypes(new CollectionField("author", "Author", "authors"));
        types.registerCollection(new CollectionType("authors", Map.of("main", FormDefinition.empty())));
        write("en/collections/authors/jane.md", "title: Jane", "");
        write("de/content/index.md", "template: page.html\nauthor: jane", "<a href='/de/authors/jane'>Jane</a>");
        var en = site("en", "/", types, Map.of("authors", new CollectionDefinition("authors", new CollectionDetailConfiguration("/authors/{id}", "author.html"))));
        var enIndex = index;
        ServiceRegistry.getInstance().register("en", SiteDBService.class, new SiteDBService(en.db()));
        site("de", "/de", types, Map.of("authors", new CollectionDefinition("authors", "en", new CollectionDetailConfiguration("/authors/{id}", "author.html"))));
        index.rebuild();
        assertThat(index.problems()).isEmpty();
        assertThat(enIndex.incoming(key("en", UsageResource.Kind.COLLECTION_ITEM, "authors/jane.md")))
                .as("a site index never aggregates sources from another site").isEmpty();
        assertThat(index.incoming(key("en", UsageResource.Kind.COLLECTION_ITEM, "authors/jane.md")))
                .as("outgoing: %s", index.outgoing(key("de", UsageResource.Kind.CONTENT, "index.md"))).hasSize(1)
                .allMatch(usage -> usage.source().site().equals("de")
                        && usage.targetStatus() == Usage.TargetStatus.UNRESOLVED);
        assertThat(index.problems()).isEmpty();
    }

    @Test void reportsMissingSchemasAndRetainsLastGoodReferencesOnParseFailure() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        var source = write("en/content/index.md", "template: page.html\nhero: first.svg", "");
        write("en/content/unknown.md", "template: unknown.html", "![Still indexed](/media/body.svg)");
        site("en", "/", types, Map.of());
        index.rebuild();
        assertThat(index.problems()).anyMatch(problem -> problem.path().equals("unknown.md"));
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "body.svg"))).hasSize(1);
        Files.writeString(source, "---\nhero: [\n---\n");
        index.refresh(source);
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "first.svg"))).hasSize(1);
        assertThat(index.problems()).anyMatch(problem -> problem.path().equals("index.md"));
    }

    @Test void updatesFromCollectionAndContentEventsAndRebuildsChangedSchemas() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        types.registerCollection(new CollectionType("authors", Map.of("main", new FormDefinition(List.of(new MediaField("portrait", "Portrait"))))));
        write("en/collections/authors/jane.md", "portrait: first.svg", "");
        write("en/content/index.md", "template: page.html\nhero: before.svg", "");
        var site = site("en", "/", types, Map.of());
        var bus = buses.get("en");
        bus.register(CollectionChangedEvent.class, event -> index.refresh(event.path()));
        bus.register(ContentChangedEvent.class, event -> index.refresh(event.contentPath()));
        index.rebuild();
        write("en/collections/authors/jane.md", "portrait: second.svg", "");
        site.db().getCollections().refresh("authors", "jane");
        await().untilAsserted(() -> {
            assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).hasSize(1);
            assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "first.svg"))).isEmpty();
        });
        write("en/content/index.md", "template: page.html\nhero: after.svg", "");
        bus.syncPublish(new ReIndexContentMetaDataEvent("index.md"));
        site.db().getFileSystem().flushContentChanges();
        await().untilAsserted(() -> assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "after.svg"))).hasSize(1));
        types.registerCollection(new CollectionType("authors", Map.of("main", FormDefinition.empty())));
        index.rebuild();
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).isEmpty();
    }

    @Test void includesSectionsVariantsTabsNestedListsAndRelativeLinksFromCustomUrls() throws Exception {
        var types = new ContentTypes();
        var main = new FormDefinition(List.of(new ListField("cards", "Cards")),
                List.of(new FormTab("Images", List.of(new MediaField("seo.image", "SEO")))));
        types.registerPageTemplate(new PageTemplate("page", "page.html", Map.of(
                "main", main,
                "cards", new FormDefinition(List.of(new MediaField("image", "Image"), new ListField("links", "Links"))),
                "links", new FormDefinition(List.of(new ReferenceField("target", "Target"))))));
        types.registerSectionEntryTemplate(new SectionEntryTemplate("hero", "hero", "hero.html",
                Map.of("main", new FormDefinition(List.of(new MediaField("image", "Image"))))));
        write("de/content/index.md", "template: page.html\nurl: /news/article\nseo:\n  image: seo.svg\ncards:\n  - image: card.svg\n    links:\n      - target: about.md\nimage: not-a-root-field.svg", "");
        write("de/content/index.hero.one.md", "template: hero.html\nimage: section.svg", "<img src='../media/relative.svg'>");
        write("de/content/about.md", "template: page.html\naliases: [/old-about]", "");
        write("de/content/variant.md", "template: page.html\nstatus: draft\nseo:\n  image: variant.svg", "<a href='/de/old-about'>Alias</a>");
        site("de", "/de", types, Map.of());
        index.rebuild();
        assertThat(index.incoming(key("de", UsageResource.Kind.MEDIA, "not-a-root-field.svg"))).isEmpty();
        assertThat(index.incoming(key("de", UsageResource.Kind.MEDIA, "seo.svg"))).hasSize(1);
        assertThat(index.incoming(key("de", UsageResource.Kind.MEDIA, "card.svg"))).hasSize(1);
        assertThat(index.incoming(key("de", UsageResource.Kind.MEDIA, "variant.svg"))).hasSize(1);
        assertThat(index.incoming(key("de", UsageResource.Kind.MEDIA, "relative.svg"))).singleElement()
                .satisfies(usage -> assertThat(usage.source().path()).isEqualTo("index.hero.one.md"));
        assertThat(index.incoming(key("de", UsageResource.Kind.CONTENT, "about.md"))).hasSize(2)
                .anyMatch(usage -> usage.location().equals("metadata.cards[0].links[0].target"));
        assertThat(index.problems()).isEmpty();
    }

    @Test void updatesMediaExistenceWithoutReparsingSourcesAndPreservesUnknownUrls() throws Exception {
        write("en/content/index.md", "template: page.html", "<img src='/media/folder/a%20b.svg?format=small#x'>"
                + "<a href='/missing'>Missing</a><a href='#here'>Same page</a>"
                + "<img srcset='data:image/svg+xml;base64,AAAA 1x, /media/real.svg 2x'>");
        site("en", "/", pageTypes(), Map.of());
        index.rebuild();
        var media = key("en", UsageResource.Kind.MEDIA, "folder/a b.svg");
        assertThat(index.incoming(media)).singleElement().satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.MISSING));
        Files.createDirectories(temp.resolve("en/assets/folder"));
        Files.writeString(temp.resolve("en/assets/folder/a b.svg"), "svg");
        assertThat(index.incoming(media)).singleElement().satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.EXISTS));
        assertThat(index.outgoing(key("en", UsageResource.Kind.CONTENT, "index.md")))
                .anyMatch(usage -> usage.target().kind() == UsageResource.Kind.UNRESOLVED_URL && usage.originalReference().equals("/missing"))
                .noneMatch(usage -> usage.originalReference().startsWith("data:") || usage.originalReference().startsWith("#"));
        assertThat(index.incoming(key("en", UsageResource.Kind.MEDIA, "real.svg"))).hasSize(1);
    }

    @Test void resolvesRelativeCollectionLinksOnlyWhenTheirPublicBaseIsKnown() throws Exception {
        var types = pageTypes();
        types.registerCollection(new CollectionType("authors", Map.of("main", FormDefinition.empty())));
        write("en/collections/authors/jane.md", "title: Jane", "<img src='portrait.svg'>");
        site("en", "/", types, Map.of());
        index.rebuild();
        assertThat(index.outgoing(key("en", UsageResource.Kind.COLLECTION_ITEM, "authors/jane.md"))).singleElement()
                .satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.UNRESOLVED));
    }

    @Test void contextReloadReevaluatesPublicUrlsWithoutStrippingTypedFieldPaths() throws Exception {
        var types = pageTypes(new ReferenceField("related", "Related"));
        write("de/content/index.md", "template: page.html\nrelated: about.md", "<a href='/de/about'>Old URL</a>");
        write("de/content/about.md", "template: page.html", "");
        var site = site("de", "/de", types, Map.of());
        index.rebuild();
        when(site.properties().contextPath()).thenReturn("/german");
        index.rebuild();
        assertThat(index.incoming(key("de", UsageResource.Kind.CONTENT, "about.md"))).hasSize(2)
                .anyMatch(usage -> usage.origin() == Usage.Origin.CONTENT_TYPE && usage.targetStatus() == Usage.TargetStatus.EXISTS)
                .anyMatch(usage -> usage.origin() == Usage.Origin.HTML && usage.targetStatus() == Usage.TargetStatus.MISSING);
    }

    @Test void reopensLuceneWithoutReextractingUnchangedSourcesOrCreatingANewCommit() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        write("en/content/index.md", "template: page.html\nhero: persistent.svg", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        index.close();
        long generation = generation(site.root());

        var unchangedTypes = new FailOnSchemaLookup();
        unchangedTypes.registerPageTemplate(new PageTemplate("page", "page.html", Map.of("main",
                new FormDefinition(List.of(new MediaField("hero", "Hero"))))));
        var reopenedSite = new UsageSite(site.id(), site.root(), site.db(), site.configuration(), () -> unchangedTypes);
        try (var reopened = new EditorialUsageIndex(reopenedSite)) {
            // Available directly from Lucene, before filesystem reconciliation.
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "persistent.svg"))).hasSize(1);
            reopened.synchronize();
            assertThat(reopened.problems()).isEmpty();
            assertThat(generation(site.root())).isEqualTo(generation);
        }
    }

    @Test void reconcilesOfflineChangesAndPreservesDeletedCustomUrlTargetsAcrossRestarts() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        write("en/content/index.md", "template: page.html\nhero: first.svg", "<a href='/special'>Target</a>");
        var target = write("en/content/target.md", "template: page.html\nurl: /special", "");
        var removed = write("en/content/removed.md", "template: page.html\nhero: removed.svg", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        index.close();
        Files.delete(target);
        Files.delete(removed);
        write("en/content/index.md", "template: page.html\nhero: second.svg", "<a href='/special'>Target</a>");
        write("en/content/added.md", "template: page.html\nhero: added.svg", "");
        ((FileDB) site.db()).reindex();
        try (var reopened = new EditorialUsageIndex(site)) {
            reopened.synchronize();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "first.svg"))).isEmpty();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "removed.svg"))).isEmpty();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).hasSize(1);
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "added.svg"))).hasSize(1);
            assertThat(reopened.incoming(key("en", UsageResource.Kind.CONTENT, "target.md"))).singleElement()
                    .satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.MISSING));
        }
        try (var again = new EditorialUsageIndex(site)) {
            again.synchronize();
            assertThat(again.incoming(key("en", UsageResource.Kind.CONTENT, "target.md"))).singleElement()
                    .satisfies(usage -> assertThat(usage.targetStatus()).isEqualTo(Usage.TargetStatus.MISSING));
            assertThat(again.problems()).isEmpty();
        }
    }

    @Test void changedSchemaInvalidatesStoredExtractionEvenWhenSourceFileIsUnchanged() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        write("en/content/index.md", "template: page.html\nhero: first.svg\nother: second.svg", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        index.close();
        var newTypes = pageTypes(new MediaField("other", "Other"));
        try (var reopened = new EditorialUsageIndex(
                new UsageSite(site.id(), site.root(), site.db(), site.configuration(), () -> newTypes))) {
            reopened.synchronize();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "first.svg"))).isEmpty();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "second.svg"))).hasSize(1);
        }
    }

    @Test void preservesDateMetadataForCollectionRoutesAfterReopening() throws Exception {
        var types = pageTypes();
        types.registerCollection(new CollectionType("events", Map.of("main", FormDefinition.empty())));
        write("en/content/index.md", "template: page.html", "<a href='/events/2026/launch'>Launch</a>");
        write("en/collections/events/launch.md", "date: 2026-09-08\nstatus: draft", "");
        var site = site("en", "/", types, Map.of("events", new CollectionDefinition("events",
                new CollectionDetailConfiguration("/events/{date:yyyy}/{id}", "event.html"))));
        index.rebuild();
        index.close();
        try (var reopened = new EditorialUsageIndex(site)) {
            reopened.synchronize();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.COLLECTION_ITEM, "events/launch.md"))).hasSize(1);
            assertThat(reopened.problems()).isEmpty();
        }
    }

    @Test void parseErrorsAndLastGoodReferencesSurviveRestartAndRecoverOnNextValidSave() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        var source = write("en/content/index.md", "template: page.html\nhero: original.svg", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        Files.writeString(source, "---\nhero: [\n---\n");
        index.refresh(source);
        index.close();
        try (var reopened = new EditorialUsageIndex(site)) {
            assertThat(reopened.problems()).anyMatch(problem -> problem.path().equals("index.md"));
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "original.svg"))).hasSize(1);
            reopened.synchronize();
            assertThat(reopened.problems()).anyMatch(problem -> problem.path().equals("index.md"));
            write("en/content/index.md", "template: page.html\nhero: recovered.svg", "");
            reopened.refresh(source);
            assertThat(reopened.problems()).isEmpty();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "original.svg"))).isEmpty();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "recovered.svg"))).hasSize(1);
        }
    }

    @Test void failedReconciliationKeepsTheCommittedLuceneGraph() throws Exception {
        var types = pageTypes(new MediaField("hero", "Hero"));
        write("en/content/index.md", "template: page.html\nhero: persistent.svg", "");
        var site = site("en", "/", types, Map.of());
        index.rebuild();
        index.close();

        var unavailableTypes = new UsageSite(site.id(), site.root(), site.db(), site.configuration(), () -> {
            throw new IllegalStateException("Content Types unavailable");
        });
        try (var reopened = new EditorialUsageIndex(unavailableTypes)) {
            reopened.synchronize();
            assertThat(reopened.incoming(key("en", UsageResource.Kind.MEDIA, "persistent.svg"))).hasSize(1);
            assertThat(reopened.problems()).anyMatch(problem -> problem.message().contains("Content Types unavailable"));
        }
    }

    private long generation(Path root) throws Exception {
        try (var directory = org.apache.lucene.store.FSDirectory.open(root.resolve("data/usage/index"));
                var reader = org.apache.lucene.index.DirectoryReader.open(directory)) {
            return reader.getIndexCommit().getGeneration();
        }
    }

    static class FailOnSchemaLookup extends ContentTypes {
        @Override public Set<PageTemplate> getPageTemplates() {
            throw new AssertionError("Unchanged source was extracted again");
        }
    }

    private ContentTypes pageTypes(FormField... fields) {
        var types = new ContentTypes();
        types.registerPageTemplate(new PageTemplate("page", "page.html", Map.of("main", new FormDefinition(List.of(fields)))));
        return types;
    }

    private UsageSite site(String id, String context, ContentTypes types, Map<String, CollectionDefinition> collections) throws Exception {
        Path root = temp.resolve(id);
        for (String folder : List.of("content", "assets", "templates", "extensions", "collections")) Files.createDirectories(root.resolve(folder));
        var properties = mock(SiteProperties.class);
        when(properties.id()).thenReturn(id);
        when(properties.contextPath()).thenReturn(context);
        when(properties.baseUrl()).thenReturn("https://example.test");
        when(properties.hostnames()).thenReturn(List.of("example.test"));
        var configuration = new Configuration();
        configuration.add(SiteConfiguration.class, new SiteConfiguration(properties));
        configuration.add(CollectionConfiguration.class, new CollectionConfiguration(collections));
        var bus = new DefaultEventBus();
        buses.put(id, bus);
        var db = new FileDB(root, bus, path -> {
            try { return new ContentFileParser(path.toString()).getHeader(); }
            catch (Exception ex) { throw new IllegalStateException(ex); }
        }, configuration);
        databases.add(db);
        db.init();
        var site = new UsageSite(id, root, db, configuration, () -> types);
        index = new EditorialUsageIndex(site);
        indexes.add(index);
        return site;
    }

    private Path write(String file, String header, String body) throws Exception {
        Path path = temp.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "---\n" + header + "\n---\n" + body);
        return path;
    }

    private UsageResource key(String site, UsageResource.Kind kind, String path) { return new UsageResource(site, kind, path); }
}
