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
import com.condation.cms.api.auth.Permissions;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.DBFileSystem;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.cms.ReadyOnlyFileSystem;
import com.condation.cms.api.ui.extensions.UIRemoteMethodExtensionPoint;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.api.utils.SectionUtil;
import com.condation.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.condation.cms.api.ui.annotations.RemoteMethod;
import com.condation.cms.api.ui.rpc.RPCException;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.modules.ui.utils.UIPathUtil;
import java.nio.file.Path;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemoteFileEnpoints extends AbstractRemoteMethodeExtension {

	private static ReadOnlyFile getBase(ReadyOnlyFileSystem fileSystem, String type) {
		return switch (type) {
			case "content" ->
				fileSystem.contentBase();
			case "assets" ->
				fileSystem.assetBase();
			default ->
				null;
		};
	}

	private static Path getWritableBase(DBFileSystem fileSystem, String type) {
		return switch (type) {
			case "content" ->
				fileSystem.resolve(Constants.Folders.CONTENT);
			case "assets" ->
				fileSystem.resolve(Constants.Folders.ASSETS);
			default ->
				null;
		};
	}

	@RemoteMethod(name = "files.list", permissions = {Permissions.CONTENT_EDIT})
	public Object list(Map<String, Object> parameters) {
		final DB db = getDB(parameters);
		
		var uri = (String) parameters.getOrDefault("uri", "");
		if (uri == null) {
			uri = "";
		}
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		var type = (String) parameters.get("type");
		var contentBase = getBase(db.getReadOnlyFileSystem(), type);

		var contentFile = contentBase.resolve(uri);

		Map<String, Object> result = new HashMap<>();
		result.put("uri", uri);

		List<File> files = new ArrayList<>();
		if (contentFile.isDirectory()) {
			try {
				if (contentFile.hasParent()) {
					var parent = contentFile.getParent();
					files.add(new Directory("..", parent.uri()));
				}
				contentFile.children().stream()
						.filter(child -> !SectionUtil.isSection(child.getFileName()))
						.map(this::map)
						.forEach(files::add);
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
		files.sort((f1, f2) -> {
			if (f1.directory() && !f2.directory()) {
				return -1;
			} else if (!f1.directory() && f2.directory()) {
				return 1;
			} else {
				return f1.name().compareToIgnoreCase(f2.name());
			}
		});

		result.put("files", files);

		return result;
	}

	@RemoteMethod(name = "files.delete", permissions = {Permissions.CONTENT_EDIT})
	public Object delete(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			var type = (String) parameters.get("type");
			var contentBase = getBase(db.getReadOnlyFileSystem(), type);

			var contentFile = contentBase.resolve(uri).resolve(name);

			var writableBase = getWritableBase(db.getFileSystem(), type);

			log.debug("deleting file {}", contentFile.uri());
			if (contentFile.isDirectory()) {
				FileUtils.deleteFolder(writableBase.resolve(uri).resolve(name));
			} else if ("assets".equals(type)) {
				Files.deleteIfExists(writableBase.resolve(uri).resolve(name));
			} else {
				var sections = db.getContent().listSections(contentFile);
				Files.deleteIfExists(writableBase.resolve(uri).resolve(name));
				sections.forEach(node -> {
					try {
						log.debug("deleting section {}", node.uri());
						FileUtils.deleteFolder(writableBase.resolve(node.uri()));
					} catch (IOException ioe) {
						log.error("error deleting file {}", node.uri(), ioe);
					}
				});
			}
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}

	@RemoteMethod(name = "files.rename", permissions = {Permissions.CONTENT_EDIT})
	public Object renameFile(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);
		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			var newName = (String) parameters.get("newName");
			var type = (String) parameters.get("type");

			if (newName == null || newName.isBlank()) {
				throw new IllegalArgumentException("newName must not be null or blank");
			}

			var contentBase = getBase(db.getReadOnlyFileSystem(), type);
			
			// check if both paths are in host directory
			contentBase.resolve(uri).resolve(name);
			contentBase.resolve(uri).resolve(newName);
			
			var writableBase = getWritableBase(db.getFileSystem(), type);

			var sourcePath = writableBase.resolve(uri).resolve(name);
			var targetPath = writableBase.resolve(uri).resolve(newName);

			log.debug("renaming from {} to {}", sourcePath, targetPath);

			if (!Files.exists(sourcePath)) {
				throw new RPCException("Source file not found: " + sourcePath);
			}
			if (Files.exists(targetPath)) {
				throw new RPCException("Target file already exists: " + targetPath);
			}

			Files.move(sourcePath, targetPath);

			if (!"assets".equals(type) && !Files.isDirectory(targetPath)) {
				var contentFile = contentBase.resolve(uri).resolve(name);
				var sections = db.getContent().listSections(contentFile);

				for (var node : sections) {
					var sourceSectionPath = writableBase.resolve(node.uri());
					var targetSectionPath = writableBase.resolve(node.uri().replace(name, newName));
					if (Files.exists(sourceSectionPath)) {
						log.debug("renaming section {} to {}", sourceSectionPath, targetSectionPath);
						Files.move(sourceSectionPath, targetSectionPath);
					}
				}
			}

			result.put("success", true);
			result.put("newName", newName);

		} catch (Exception e) {
			log.error("Error during rename", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}

	@RemoteMethod(name = "folders.create", permissions = {Permissions.CONTENT_EDIT})
	public Object createFolder(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		try {
			var name = (String) parameters.getOrDefault("name", "");
			var uri = (String) parameters.getOrDefault("uri", "");
			var type = (String) parameters.get("type");
			var contentBase = getWritableBase(db.getFileSystem(), type);

			name = UIPathUtil.slugify(name);
			
			Path newFile = contentBase.resolve(uri).resolve(name);
			if (newFile.isAbsolute()) {
				throw new RPCException(1, "absolut path is not supported");
			} else if (Files.exists(newFile)) {
				throw new RPCException(1, "directory already exists");
			} else if (!PathUtil.isChild(contentBase, newFile)) {
				throw new RPCException(1, "invalid path");
			}
			Files.createDirectories(newFile);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}

	@RemoteMethod(name = "files.create", permissions = {Permissions.CONTENT_EDIT})
	public Object createFile(Map<String, Object> parameters) throws RPCException {
		final DB db = getDB(parameters);

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name = (String) parameters.getOrDefault("name", "");
			var type = (String) parameters.get("type");
			var contentBase = getWritableBase(db.getFileSystem(), type);

			name = UIPathUtil.slugify(name);
			
			Path newFile = contentBase.resolve(uri).resolve(name);
			if (newFile.isAbsolute()) {
				throw new RPCException(1, "absolut path is not supported");
			} else if (Files.exists(newFile)) {
				throw new RPCException(1, "file already exists");
			} else if (!PathUtil.isChild(contentBase, newFile)) {
				throw new RPCException(1, "invalid path");
			}
			Files.createDirectories(newFile.getParent());
			Files.createFile(newFile);
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}

	private boolean isMedia (String filename) {
		var name = filename.toLowerCase();
		return name.endsWith(".jpg")
				|| name.endsWith(".jpeg")
				|| name.endsWith(".webp")
				|| name.endsWith(".png")
				|| name.endsWith(".svg")
				|| name.endsWith(".gif");
	}
	
	private File map (ReadOnlyFile readOnlyFile) {
		if (readOnlyFile.isDirectory()) {
			return new Directory(
						readOnlyFile.getFileName(),
						readOnlyFile.uri()
			);
		} else if (isMedia(readOnlyFile.getFileName())) {
			return new Media(
					readOnlyFile.getFileName(), 
					readOnlyFile.uri()
			);
		} else {
			return new Content(
					readOnlyFile.getFileName(), 
					readOnlyFile.uri()
			);
		}
	}
	
	public record Content(String name, String uri) implements File {
	}
	
	public record Media(String name, String uri) implements File {
		@Override
		public boolean media() {
			return true;
		}		
	}
	
	public record Directory (String name, String uri) implements File {
		@Override
		public boolean directory() {
			return true;
		}
	}
	
	public static interface File {
		String name ();
		String uri ();
		default boolean directory () {
			return false;
		}
		default boolean content () {
			return name().endsWith(".md");
		}
		default boolean media () {
			return false;
		}
	}
}
