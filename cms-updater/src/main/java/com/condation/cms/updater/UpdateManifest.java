package com.condation.cms.updater;

/*-
 * #%L
 * CondationCMS Updater
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

import java.util.List;

/** Description of the files and migrations in an update ZIP. */
public record UpdateManifest(String product, String version, String minimumVersion,
        List<UpdateFile> files, List<MigrationEntry> migrations,
        List<String> replaceDirectories, List<String> removePatterns) {
    public UpdateManifest(String product, String version, String minimumVersion,
            List<UpdateFile> files, List<MigrationEntry> migrations) {
        this(product, version, minimumVersion, files, migrations, List.of(), List.of());
    }

    public record UpdateFile(String source, String target, String sha256, boolean executable) {
        public UpdateFile(String source, String target, String sha256) {
            this(source, target, sha256, false);
        }
    }

    /** Entries are executed in manifest order. */
    public record MigrationEntry(String id, String artifact, String sha256) { }
}
