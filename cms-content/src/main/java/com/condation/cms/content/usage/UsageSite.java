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
import com.condation.cms.api.configuration.configs.CollectionConfiguration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.ui.elements.ContentTypes;
import java.nio.file.Path;
import java.util.function.Supplier;

/** Live site configuration and a request-independent loader for editor schemas. */
public record UsageSite(String id, Path root, DB db, Configuration configuration,
        Supplier<ContentTypes> contentTypes) {
    public UsageSite {
        root = root.toAbsolutePath().normalize();
    }

    public SiteProperties properties() {
        return configuration.get(SiteConfiguration.class).siteProperties();
    }

    public CollectionConfiguration collections() {
        return configuration.get(CollectionConfiguration.class);
    }

    public String collectionSite(String collection) {
        var config = collections();
        return config == null ? id : config.collection(collection)
                .flatMap(definition -> definition.sourceSite()).orElse(id);
    }
}
