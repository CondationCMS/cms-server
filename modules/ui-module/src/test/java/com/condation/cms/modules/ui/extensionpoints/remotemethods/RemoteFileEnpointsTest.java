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

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.core.content.io.ContentFileParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author thmar
 */
@ExtendWith(MockitoExtension.class)
public class RemoteFileEnpointsTest {
	
	@Mock
	SiteModuleContext moduleContext;
	
	@Mock
	private DB db;
	
	@Mock
	private Path basePath;
	
	@Mock
	private DBFileSystem dbFileSystem;

	@TempDir
	private Path tempDir;
	
	public RemoteFileEnpointsTest() {
	}

	@Test
	void rejectsUnsupportedFileTypesInsteadOfReturningNullBases() {
		Assertions.assertThatThrownBy(() -> AbstractRemoteMethodeExtension.getBase(dbFileSystem, "unknown"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Unsupported file type: unknown");
		Assertions.assertThatThrownBy(() -> AbstractRemoteMethodeExtension.getWritableBase(dbFileSystem, "unknown"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Unsupported file type: unknown");
		Assertions.assertThatThrownBy(() -> AbstractRemoteMethodeExtension.getBase(dbFileSystem, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Unsupported file type: null");
	}

	@Test
	public void create_folder_with_absolut_path_throws_error() throws RPCException {
		
		Mockito.when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		Mockito.when(db.getFileSystem()).thenReturn(dbFileSystem);
		Mockito.when(dbFileSystem.resolve(Constants.Folders.CONTENT)).thenReturn(Path.of("."));
		
		
		RemoteFileEnpoints fileEndpoints = new RemoteFileEnpoints();
		fileEndpoints.setContext(moduleContext);
		
		Assertions.assertThatThrownBy(() -> fileEndpoints.createFolder(Map.of(
				"type", "content",
				"uri", "/test/absolut/path"
		))).isInstanceOf(RPCException.class);//.hasMessage("invalid path");
		
	}

	@Test
	void listUsesContentTitleButKeepsTechnicalName() throws Exception {
		var content = Mockito.mock(Content.class);
		var contentBase = Mockito.mock(ReadOnlyFile.class);
		var page = Mockito.mock(ReadOnlyFile.class);
		var node = new ContentNode(
				"about.md",
				"/about",
				"about.md",
				Map.of(Constants.MetaFields.TITLE, "About us")
		);
		Mockito.when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		Mockito.when(db.getFileSystem()).thenReturn(dbFileSystem);
		Mockito.when(db.getContent()).thenReturn(content);
		Mockito.when(dbFileSystem.contentBase()).thenReturn(contentBase);
		Mockito.when(contentBase.resolve("")).thenReturn(contentBase);
		Mockito.when(contentBase.isDirectory()).thenReturn(true);
		Mockito.when(contentBase.children()).thenReturn(List.of(page));
		Mockito.when(page.getFileName()).thenReturn("about.md");
		Mockito.when(page.uri()).thenReturn("/about");
		Mockito.when(page.relativePath()).thenReturn("about.md");
		Mockito.when(content.byPath("about.md")).thenReturn(Optional.of(node));

		var endpoints = new RemoteFileEnpoints();
		endpoints.setContext(moduleContext);

		@SuppressWarnings("unchecked")
		var result = (Map<String, Object>) endpoints.list(Map.of(
				"type", "content",
				"uri", ""
		));
		var files = (List<RemoteFileEnpoints.File>) result.get("files");

		Assertions.assertThat(files).singleElement().satisfies(file -> {
			Assertions.assertThat(file.name()).isEqualTo("about.md");
			Assertions.assertThat(file.displayName()).isEqualTo("About us");
			Assertions.assertThat(((RemoteFileEnpoints.Content) file).url()).isEqualTo("/about");
			Assertions.assertThat(((RemoteFileEnpoints.Content) file).title()).isEqualTo("About us");
		});
	}

	@Test
	void renameMarkdownContentUpdatesTitleWithoutChangingFileName() throws Exception {
		var contentBase = Mockito.mock(ReadOnlyFile.class);
		var directory = Mockito.mock(ReadOnlyFile.class);
		var contentFile = Mockito.mock(ReadOnlyFile.class);
		var eventBus = Mockito.mock(EventBus.class);
		var page = tempDir.resolve("about.md");
		Files.writeString(page, "---\ntitle: Old title\ntemplate: page\n---\n\nBody");
		Mockito.when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		Mockito.when(moduleContext.get(EventBusFeature.class)).thenReturn(new EventBusFeature(eventBus));
		Mockito.when(db.getFileSystem()).thenReturn(dbFileSystem);
		Mockito.when(dbFileSystem.contentBase()).thenReturn(contentBase);
		Mockito.when(dbFileSystem.resolve(Constants.Folders.CONTENT)).thenReturn(tempDir);
		Mockito.when(contentBase.resolve("")).thenReturn(directory);
		Mockito.when(directory.resolve("about.md")).thenReturn(contentFile);

		var endpoints = new RemoteFileEnpoints();
		endpoints.setContext(moduleContext);
		endpoints.renameFile(Map.of(
				"type", "content",
				"uri", "",
				"name", "about.md",
				"newName", "New title"
		));

		Assertions.assertThat(page).exists();
		Assertions.assertThat(tempDir.resolve("New title")).doesNotExist();
		var parser = new ContentFileParser(page.toString());
		Assertions.assertThat(parser.getHeader())
				.containsEntry(Constants.MetaFields.TITLE, "New title")
				.containsEntry(Constants.MetaFields.TEMPLATE, "page");
		Assertions.assertThat(parser.getContent()).isEqualTo("Body");
		Mockito.verify(dbFileSystem).flushContentChanges();
	}
	
}
