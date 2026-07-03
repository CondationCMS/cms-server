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
import com.condation.cms.api.db.taxonomy.Taxonomies;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RemoteTaxonomyEnpointsTest {

    @Mock
    private SiteModuleContext moduleContext;

    @Mock
    private DB db;

    @Mock
    private Taxonomies taxonomies;

    private RemoteTaxonomyEnpoints endpoints;

    @BeforeEach
    public void setUp() {
        endpoints = new RemoteTaxonomyEnpoints();
        endpoints.setContext(moduleContext);
        when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
        when(db.getTaxonomies()).thenReturn(taxonomies);
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
}
