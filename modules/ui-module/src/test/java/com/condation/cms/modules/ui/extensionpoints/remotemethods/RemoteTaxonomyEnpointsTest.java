package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.taxonomy.Taxonomies;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RemoteTaxonomyEnpointsTest {

    @Mock
    private SiteModuleContext moduleContext;

    @Mock
    private DB db;

    @Mock
    private Taxonomies taxonomies;

    @Mock
    private DBFileSystem fileSystem;

    @TempDir
    private Path tempDir;

    private RemoteTaxonomyEnpoints endpoints;

    @BeforeEach
    public void setUp() {
        endpoints = new RemoteTaxonomyEnpoints();
        endpoints.setContext(moduleContext);
        when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
        when(db.getTaxonomies()).thenReturn(taxonomies);
        lenient().when(db.getFileSystem()).thenReturn(fileSystem);
        lenient().when(fileSystem.hostBase()).thenReturn(tempDir);
    }

    @Test
    public void testGet_returnsTaxonomySlugToTitleMap() throws Exception {
        Taxonomy t1 = new Taxonomy("Categories", "categories", "category");
        Taxonomy t2 = new Taxonomy("Tags", "tags", "tag");
        when(taxonomies.all()).thenReturn(List.of(t1, t2));

        Map<String, Object> params = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.get(params);

        assertThat(result).containsEntry("categories", "Categories");
        assertThat(result).containsEntry("tags", "Tags");
    }

    @Test
    public void testGet_emptyWhenNoTaxonomies() throws Exception {
        when(taxonomies.all()).thenReturn(List.of());

        Map<String, Object> params = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.get(params);

        assertThat(result).isEmpty();
    }

    @Test
    public void testValues_returnsIdAndTitleForEachValue() {
        Map<String, Value> values = new HashMap<>();
        values.put("cat1", new Value("cat1", "Category One"));
        values.put("cat2", new Value("cat2", "Category Two"));

        Taxonomy taxonomy = new Taxonomy("Categories", "categories", "category");
        taxonomy.setValues(values);

        when(taxonomies.forSlug("categories")).thenReturn(Optional.of(taxonomy));

        Map<String, Object> params = new HashMap<>();
        params.put("slug", "categories");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.remove(params);

        assertThat(result).containsKey("cat1");
        @SuppressWarnings("unchecked")
        Map<String, String> entry = (Map<String, String>) result.get("cat1");
        assertThat(entry).containsEntry("id", "cat1");
        assertThat(entry).containsEntry("title", "Category One");
    }

    @Test
    public void testValues_emptyWhenSlugNotFound() {
        when(taxonomies.forSlug("unknown")).thenReturn(Optional.empty());

        Map<String, Object> params = new HashMap<>();
        params.put("slug", "unknown");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.remove(params);

        assertThat(result).isEmpty();
    }

    @Test
    public void createValue_writesTaxonomyYamlValue() throws Exception {
        Taxonomy taxonomy = new Taxonomy("Tags", "tags", "taxonomy.tags");
        taxonomy.setValues(new HashMap<>());
        when(taxonomies.forSlug("tags")).thenReturn(Optional.of(taxonomy));

        Map<String, Object> params = new HashMap<>();
        params.put("slug", "tags");
        params.put("title", "Hoodies & Sweatshirts");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.createValue(params);

        assertThat(result).containsEntry("id", "hoodies-sweatshirts");
        assertThat(result).containsEntry("title", "Hoodies & Sweatshirts");

        var taxonomyFile = tempDir.resolve("config/taxonomy.tags.yaml");
        assertThat(readTaxonomyValues(taxonomyFile))
                .contains(Map.of("id", "hoodies-sweatshirts", "title", "Hoodies & Sweatshirts"));
    }

    @Test
    public void createValue_preservesExistingTaxonomyYamlValues() throws Exception {
        var configDir = tempDir.resolve("config");
        Files.createDirectories(configDir);
        var taxonomyFile = configDir.resolve("taxonomy.tags.yaml");
        Files.writeString(taxonomyFile, """
                ---
                values:
                  - id: existing
                    title: Existing
                """);

        Taxonomy taxonomy = new Taxonomy("Tags", "tags", "taxonomy.tags");
        taxonomy.setValues(new HashMap<>());
        when(taxonomies.forSlug("tags")).thenReturn(Optional.of(taxonomy));

        Map<String, Object> params = new HashMap<>();
        params.put("slug", "tags");
        params.put("title", "New Tag");

        endpoints.createValue(params);

        assertThat(readTaxonomyValues(taxonomyFile))
                .contains(Map.of("id", "existing", "title", "Existing"))
                .contains(Map.of("id", "new-tag", "title", "New Tag"));
    }

    @Test
    public void createValue_doesNotWriteExistingTaxonomyValue() throws Exception {
        Map<String, Value> values = new HashMap<>();
        values.put("existing", new Value("existing", "Existing"));

        Taxonomy taxonomy = new Taxonomy("Tags", "tags", "taxonomy.tags");
        taxonomy.setValues(values);
        when(taxonomies.forSlug("tags")).thenReturn(Optional.of(taxonomy));

        Map<String, Object> params = new HashMap<>();
        params.put("slug", "tags");
        params.put("title", "Existing");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) endpoints.createValue(params);

        assertThat(result).containsEntry("id", "existing");
        assertThat(Files.exists(tempDir.resolve("config/taxonomy.tags.yaml"))).isFalse();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> readTaxonomyValues(Path taxonomyFile) throws Exception {
        Map<String, Object> document = new Yaml().load(Files.readString(taxonomyFile));
        return (List<Map<String, String>>) document.get("values");
    }
}
