package com.github.thmarx.cms.modules.ui.services;

/*-
 * #%L
 * ui-module
 * %%
 * Copyright (C) 2023 Marx-Software
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
import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.db.DB;
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

	private final DB db;

	public List<Node> listContent(String parent) {

		try {
			var contentBase = db.getFileSystem().resolve("content");
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
		var contentBase = db.getFileSystem().resolve("content");
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

	public String readFromFile(final String name) throws IOException {
		var contentBase = db.getFileSystem().resolve("content");
		final Path file = contentBase.resolve(name);
		if (!PathUtil.isChild(contentBase, file)) {
			return "";
		}

		if (Files.exists(file)) {
			return Files.readString(file, StandardCharsets.UTF_8);
		}
		return "";
	}
	
	public boolean writeToFile(final String name, final String content) throws IOException {
		var contentBase = db.getFileSystem().resolve("content");
		final Path file = contentBase.resolve(name);
		if (!PathUtil.isChild(contentBase, file)) {
			return false;
		}

		if (Files.exists(file)) {
			Files.writeString(file, content, StandardCharsets.UTF_8);
		}
		return true;
	}

	public void delete(String path) throws IOException {
		var contentBase = db.getFileSystem().resolve("content");
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
		var contentBase = db.getFileSystem().resolve("content");
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
