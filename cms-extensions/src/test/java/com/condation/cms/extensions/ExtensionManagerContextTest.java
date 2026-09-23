package com.condation.cms.extensions;

/*-
 * #%L
 * CMS Extensions
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

import com.condation.cms.api.ServerProperties;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.request.RequestContext;
import com.condation.cms.api.theme.Theme;
import java.nio.file.Files;
import java.nio.file.Path;
import org.graalvm.polyglot.Engine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtensionManagerContextTest {

    @TempDir
    Path site;

    @Test
    void evaluatesExtensionsInFreshContextForEachRequest() throws Exception {
        Path extensions = Files.createDirectory(site.resolve("extensions"));
        Files.writeString(extensions.resolve("state.js"),
                "globalThis.extensionMarker = (globalThis.extensionMarker || 0) + 1;");

        DBFileSystem fileSystem = mock(DBFileSystem.class);
        when(fileSystem.resolve("extensions/")).thenReturn(extensions);
        DB db = mock(DB.class);
        when(db.getFileSystem()).thenReturn(fileSystem);
        ServerProperties serverProperties = mock(ServerProperties.class);
        when(serverProperties.env()).thenReturn("test");
        Theme theme = mock(Theme.class);
        when(theme.empty()).thenReturn(true);

        try (Engine engine = Engine.create()) {
            ExtensionManager manager = new ExtensionManager(db, serverProperties, engine);
            try (var first = manager.newContext(theme, new RequestContext())) {
                assertEquals(1, first.getContext().getBindings("js").getMember("extensionMarker").asInt());
                assertTrue(first.getContext().eval("js", "Java.type('java.time.Instant') != null").asBoolean());
                first.getContext().eval("js", "globalThis.extensionMarker = 42");
            }
            try (var second = manager.newContext(theme, new RequestContext())) {
                assertEquals(1, second.getContext().getBindings("js").getMember("extensionMarker").asInt());
            }
        }
    }
}
