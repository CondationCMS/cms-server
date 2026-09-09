package com.condation.cms.api.usage;

/*-
 * #%L
 * CMS Api
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

import java.util.Objects;

/** Identity within a site's content, assets or collections root; never a public context path. */
public record UsageResource(String site, Kind kind, String path) {
    public enum Kind { CONTENT, MEDIA, COLLECTION_ITEM, UNRESOLVED_URL }

    public UsageResource {
        Objects.requireNonNull(site, "site");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(path, "path");
        path = path.replace('\\', '/').replaceAll("^/+", "");
        if (site.isBlank() || path.contains("\u0000")) {
            throw new IllegalArgumentException("invalid usage resource");
        }
        if (kind != Kind.UNRESOLVED_URL) {
            path = java.nio.file.Path.of(path).normalize().toString().replace('\\', '/');
            if (path.isBlank() || path.equals("..") || path.startsWith("../")) {
                throw new IllegalArgumentException("invalid site-local resource path");
            }
        }
    }
}
