package com.condation.cms.filesystem;

/*-
 * #%L
 * CMS FileSystem
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.condation.cms.api.Constants;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.EventListener;
import com.condation.cms.api.eventbus.events.ContentChangedEvent;
import com.condation.cms.api.eventbus.events.InvalidateContentCacheEvent;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.yaml.snakeyaml.Yaml;

public class FileSystemIncrementalEventTest {

	@TempDir
	Path tempDirectory;

	@Test
	public void directoryRenameReindexesOnlyTheMovedSubtree() throws Exception {
		var content = tempDirectory.resolve("content");
		var oldDirectory = content.resolve("old");
		Files.createDirectories(oldDirectory.resolve("nested"));
		Files.createDirectories(content.resolve("unaffected"));
		writePage(oldDirectory.resolve("page.md"), "/stable-page");
		writePage(oldDirectory.resolve("nested/child.md"), "/stable-child");
		writePage(content.resolve("unaffected/page.md"), "/unaffected");

		var fileSystem = new FileSystem("test-site", tempDirectory, Mockito.mock(EventBus.class), file -> {
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		try {
			fileSystem.init();
			var newDirectory = content.resolve("new");
			Files.createDirectories(newDirectory.resolve("nested"));
			writePage(newDirectory.resolve("page.md"), "/stable-page");
			writePage(newDirectory.resolve("nested/child.md"), "/stable-child");
			Files.delete(oldDirectory.resolve("nested/child.md"));
			Files.delete(oldDirectory.resolve("nested"));
			Files.delete(oldDirectory.resolve("page.md"));
			Files.delete(oldDirectory);

			fileSystem.handleContentEvent(new FileEvent(oldDirectory.toFile(), FileEvent.Type.DELETED));
			fileSystem.handleContentEvent(new FileEvent(newDirectory.toFile(), FileEvent.Type.CREATED));
			fileSystem.flushContentChanges();

			Assertions.assertThat(fileSystem.getMetaData().byPath("old/page.md")).isEmpty();
			Assertions.assertThat(fileSystem.getMetaData().byPath("old/nested/child.md")).isEmpty();
			Assertions.assertThat(fileSystem.getMetaData().byPath("new/page.md")).isPresent();
			Assertions.assertThat(fileSystem.getMetaData().byPath("new/nested/child.md")).isPresent();
			Assertions.assertThat(fileSystem.getMetaData().byPath("unaffected/page.md")).isPresent();
			Assertions.assertThat(fileSystem.getMetaData().byUrl("/stable-page"))
					.get()
					.extracting(node -> node.path())
					.isEqualTo("new/page.md");
		} finally {
			fileSystem.shutdown();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void duplicateWatcherAndExplicitEventsAreProcessedAsOneBatch() throws Exception {
		var content = tempDirectory.resolve("content");
		Files.createDirectories(content);
		var files = new Path[] {
			content.resolve("index.asection.bla.md"),
			content.resolve("index.asection.test.md"),
			content.resolve("index.asection.test1.md"),
			content.resolve("index.asection.other.md")
		};
		for (var file : files) {
			writePage(file, "/page");
		}

		var parseCount = new AtomicInteger();
		var eventBus = Mockito.mock(EventBus.class);
		var fileSystem = new FileSystem("test-site", tempDirectory, eventBus, file -> {
			parseCount.incrementAndGet();
			try {
				return new Yaml().load(Files.readString(file));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		try {
			fileSystem.init();
			var listenerCaptor = org.mockito.ArgumentCaptor.forClass(EventListener.class);
			Mockito.verify(eventBus).register(
					Mockito.eq(ReIndexContentMetaDataEvent.class), listenerCaptor.capture());
			var reindexListener = (EventListener<ReIndexContentMetaDataEvent>) listenerCaptor.getValue();
			parseCount.set(0);
			Mockito.clearInvocations(eventBus);

			for (var file : files) {
				var uri = content.relativize(file).toString().replace('\\', '/');
				reindexListener.consum(new ReIndexContentMetaDataEvent(uri));
				fileSystem.handleContentEvent(new FileEvent(file.toFile(), FileEvent.Type.MODIFIED));
				fileSystem.handleContentEvent(new FileEvent(file.toFile(), FileEvent.Type.MODIFIED));
			}
			fileSystem.flushContentChanges();

			Assertions.assertThat(parseCount).hasValue(4);
			Mockito.verify(eventBus, Mockito.times(4)).publish(Mockito.any(ContentChangedEvent.class));
			Mockito.verify(eventBus, Mockito.times(1)).publish(Mockito.any(InvalidateContentCacheEvent.class));
		} finally {
			fileSystem.shutdown();
		}
	}

	private static void writePage(Path path, String url) throws Exception {
		Files.writeString(path, "status: published%n%s: %s%n".formatted(Constants.MetaFields.URL, url));
	}
}
