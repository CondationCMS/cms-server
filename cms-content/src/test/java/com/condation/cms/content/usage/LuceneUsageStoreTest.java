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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

class LuceneUsageStoreTest {
    @TempDir Path root;

    @Test void queriesAllHitsAcrossPagesAndAtomicallyReplacesBothDirections() throws Exception {
        var target = new UsageResource("de", UsageResource.Kind.MEDIA, "image.svg");
        var replacement = new UsageResource("de", UsageResource.Kind.MEDIA, "replacement.svg");
        try (var store = new LuceneUsageStore(root)) {
            for (int i = 0; i < 300; i++) store.update(source("page" + i + ".md", target));
            store.commit(List.of());
            assertThat(store.incoming(target)).hasSize(300);
            store.update(source("page0.md", replacement));
            // Readers see the last committed batch until both directions have been committed.
            assertThat(store.incoming(target)).hasSize(300);
            assertThat(store.incoming(replacement)).isEmpty();
            store.commit(List.of());
            assertThat(store.incoming(target)).hasSize(299);
            assertThat(store.incoming(replacement)).hasSize(1);
        }
        try (var store = new LuceneUsageStore(root)) {
            assertThat(store.sources()).hasSize(300);
            assertThat(store.incoming(target)).hasSize(299);
            var source = new UsageResource("en", UsageResource.Kind.CONTENT, "page0.md");
            assertThat(store.outgoing(source)).singleElement().satisfies(usage -> assertThat(usage.target()).isEqualTo(replacement));
            store.delete(source);
            store.commit(List.of());
            assertThat(store.incoming(replacement)).isEmpty();
            assertThat(store.outgoing(source)).isEmpty();
        }
    }

    @Test void resolvesAliasesAndCollectionRoutesFromLucene() throws Exception {
        var page = new UsageResource("en", UsageResource.Kind.CONTENT, "page.md");
        var item = new UsageResource("en", UsageResource.Kind.COLLECTION_ITEM, "events/launch.md");
        var duplicate = new UsageResource("en", UsageResource.Kind.CONTENT, "duplicate.md");
        try (var store = new LuceneUsageStore(root)) {
            store.update(new PersistedUsageSource(
                    new UsageDocument(page, "/page", Map.of("aliases", List.of("/special")), List.of()),
                    "stamp", "schema", List.of(), List.of(), List.of()));
            store.update(new PersistedUsageSource(
                    new UsageDocument(item, "/events/2026/launch", Map.of("title", "Launch"), List.of()),
                    "stamp", "schema", List.of(), List.of(), List.of()));
            store.commit(List.of());

            assertThat(store.source(page)).get().extracting(PersistedUsageSource::document)
                    .isEqualTo(new UsageDocument(page, "/page", Map.of("aliases", List.of("/special")), List.of()));
            assertThat(store.aliasTarget("special")).contains(page);
            assertThat(store.collectionTarget("/events/2026/launch/")).contains(item);

            store.update(new PersistedUsageSource(
                    new UsageDocument(duplicate, "/duplicate", Map.of("aliases", List.of("/special")), List.of()),
                    "stamp", "schema", List.of(), List.of(), List.of()));
            store.commit(List.of());
            assertThat(store.aliasTarget("/special")).as("ambiguous aliases are unresolved").isEmpty();
        }
    }

    private PersistedUsageSource source(String path, UsageResource target) {
        var source = new UsageResource("en", UsageResource.Kind.CONTENT, path);
        var document = new UsageDocument(source, "/", Map.of("title", "Page"), List.of());
        var usage = new Usage(source, target, "metadata.image", Usage.Origin.CONTENT_TYPE, target.path(),
                "Page", "draft", Usage.TargetStatus.EXISTS);
        return new PersistedUsageSource(document, "stamp", "schema", List.of(usage), List.of(), List.of());
    }
}
