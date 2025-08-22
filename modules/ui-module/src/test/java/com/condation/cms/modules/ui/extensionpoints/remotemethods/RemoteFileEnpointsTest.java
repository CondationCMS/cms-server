package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.condation.cms.api.Constants;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import java.nio.file.Path;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	
	public RemoteFileEnpointsTest() {
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
	
}
