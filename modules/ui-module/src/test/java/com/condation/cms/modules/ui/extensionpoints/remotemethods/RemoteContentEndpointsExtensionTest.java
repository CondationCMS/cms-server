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
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteContentEndpointsExtensionTest {

	@Mock
	private SiteModuleContext moduleContext;

	@Mock
	private DB db;

	@Mock
	private DBFileSystem fileSystem;

	@Mock
	private ReadOnlyFile contentBase;

	@Mock
	private ReadOnlyFile contentFile;

	private RemoteContentEndpointsExtension endpoints;

	@BeforeEach
	void setUp() {
		endpoints = new RemoteContentEndpointsExtension();
		endpoints.setContext(moduleContext);
		when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
		when(db.getFileSystem()).thenReturn(fileSystem);
		when(fileSystem.contentBase()).thenReturn(contentBase);
	}

	@Test
	void getContent_throwsRPCException_whenParsingFails() throws IOException {
		when(contentBase.resolve("broken.md")).thenReturn(contentFile);
		when(contentFile.getContent()).thenThrow(new IOException("disk error"));

		Map<String, Object> params = Map.of("uri", "broken.md");

		assertThatThrownBy(() -> endpoints.getContent(params))
				.isInstanceOf(RPCException.class)
				.hasMessage("disk error");
	}

	@Test
	void setContent_throwsRPCException_whenParsingFails() throws IOException {
		when(contentBase.resolve("broken.md")).thenReturn(contentFile);
		when(contentFile.getContent()).thenThrow(new IOException("disk error"));

		Map<String, Object> params = Map.of("uri", "broken.md", "content", "hello");

		assertThatThrownBy(() -> endpoints.setContent(params))
				.isInstanceOf(RPCException.class)
				.hasMessage("disk error");
	}
}
