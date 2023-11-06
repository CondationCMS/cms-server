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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

	public List<Node> listContent(String parent) {

		try {
			var contentBase = fileSystem.resolve("content");
			final Path parentNode = contentBase.resolve(parent);
			if (!PathUtil.isChild(contentBase, parentNode)) {
				return Collections.emptyList();
			}

			return Files.list(parentNode).map((path) -> {
				var uri = PathUtil.toUri(path, contentBase);
				return new Node(uri, path.getFileName().toString(), PathUtil.getType(path), PathUtil.hasChildren(path));
			}).toList();
		} catch (IOException ex) {
			log.error(null, ex);
		}
		return Collections.emptyList();
	}

	public boolean createFile(final String name, final String parent, final String content) throws IOException {
		var contentBase = fileSystem.resolve("content");
		final Path parentNode = contentBase.resolve(parent);
		if (!PathUtil.isChild(contentBase, parentNode)) {
			return false;
		}

		Path resolve = parentNode.resolve(name);
		if (!Files.exists(resolve)) {
			Files.createFile(resolve);
			Files.writeString(resolve, content, StandardCharsets.UTF_8);
		}

		return true;
	}

	public void delete(String path) throws IOException {
		var contentBase = fileSystem.resolve("content");
		var pathToBeDeleted = contentBase.resolve(path);
		if (!Files.isDirectory(pathToBeDeleted)) {
			Files.deleteIfExists(pathToBeDeleted);
		}
		Files.walk(pathToBeDeleted)
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	public boolean createFolder(final String name, final String parent) throws IOException {
		var contentBase = fileSystem.resolve("content");
		final Path parentNode = contentBase.resolve(parent);
		if (!PathUtil.isChild(contentBase, parentNode)) {
			return false;
		}

		Path resolve = parentNode.resolve(name);
		if (!Files.exists(resolve)) {
			Files.createDirectory(resolve);
		}

		return true;
	}

	public static record Node(String id, String text, String type, boolean children) {

	}
}
