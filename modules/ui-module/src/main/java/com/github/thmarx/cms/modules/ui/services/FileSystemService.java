package com.github.thmarx.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.modules.ui.utils.PathUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class FileSystemService {
	private final ModuleFileSystem fileSystem;
	
	public List<Node> listContent (String parent) {
		
		try {
			var contentBase = fileSystem.resolve("content");
			
			return Files.list(contentBase.resolve(parent)).map((path) -> {
				var uri = PathUtil.toUri(path, contentBase);
				return new Node(uri, path.getFileName().toString(), getIcon(path), hasChildren(path));
			}).toList();
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return Collections.emptyList();
	}
	
	private boolean hasChildren (Path path) {
		try {
			return Files.list(path).count() > 0;
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return false;
	}
	
	private String getIcon (Path path) {
		if (Files.isDirectory(path)) {
			return "folder";
		} else {
			return "file";
		}
	}
	
	public static record Node (String id, String text, String icon, boolean children){}
}
