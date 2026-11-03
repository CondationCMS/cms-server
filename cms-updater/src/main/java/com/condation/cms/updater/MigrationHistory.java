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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

/** Stores completed migration IDs outside replaceable program files. */
final class MigrationHistory {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final Set<String> completed;

    private MigrationHistory(Path file, Set<String> completed) {
        this.file = file;
        this.completed = completed;
    }

    static MigrationHistory load(Path installationDirectory) throws IOException {
        Path file = installationDirectory.resolve("data/system/update-history.json");
        if (!Files.exists(file)) {
            return new MigrationHistory(file, new LinkedHashSet<>());
        }
        HistoryDocument document = GSON.fromJson(Files.readString(file), HistoryDocument.class);
        if (document == null || document.migrations == null) {
            throw new IOException("Invalid update history: " + file);
        }
        return new MigrationHistory(file, new LinkedHashSet<>(document.migrations));
    }

    boolean contains(String id) {
        return completed.contains(id);
    }

    void record(String id) throws IOException {
        completed.add(id);
        Files.createDirectories(file.getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(new HistoryDocument(completed)));
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveUnavailable) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record HistoryDocument(Set<String> migrations) { }
}
