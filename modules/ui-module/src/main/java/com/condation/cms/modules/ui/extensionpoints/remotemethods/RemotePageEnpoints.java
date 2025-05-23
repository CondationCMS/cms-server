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
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.cms.ReadyOnlyFileSystem;
import com.condation.cms.api.feature.features.DBFeature;
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
import com.condation.cms.modules.ui.utils.YamlHeaderUpdater;
import java.nio.file.Path;

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteMethodExtensionPoint.class)
public class RemotePageEnpoints extends UIRemoteMethodExtensionPoint {

	@RemoteMethod(name = "page.delete")
	public Object deletePage(Map<String, Object> parameters) throws RPCException {
		final DB db = getContext().get(DBFeature.class).db();

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var contentBase = db.getReadOnlyFileSystem().contentBase();

			var contentFile = contentBase.resolve(uri);

			log.debug("deleting file {}", contentFile.uri());
			if (contentFile.isDirectory()) {
				FileUtils.deleteFolder(db.getFileSystem().resolve(uri));
			} else {
				var sections = db.getContent().listSections(contentFile);
				Files.deleteIfExists(db.getFileSystem().resolve(uri));
				sections.forEach(node -> {
					try {
						log.debug("deleting section {}", node.uri());
						FileUtils.deleteFolder(db.getFileSystem().resolve(node.uri()));
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

	@RemoteMethod(name = "page.create")
	public Object createPage(Map<String, Object> parameters) throws RPCException {
		final DB db = getContext().get(DBFeature.class).db();

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var name  = (String) parameters.getOrDefault("name", "");
			var meta = (Map<String, Object>) parameters.getOrDefault("meta", Map.of());
			var contentBase = db.getFileSystem().resolve(Constants.Folders.CONTENT);

			Path newFile = contentBase.resolve(uri).resolve(name);
			if (newFile.isAbsolute()) {
				throw new RPCException(1, "absolut path is not supported");
			} else if (Files.exists(newFile)) {
				throw new RPCException(1, "directory already exists");
			} else if (!PathUtil.isChild(contentBase, newFile)) {
				throw new RPCException(1, "invalid path");
			}
			Files.createDirectories(newFile.getParent());
			Files.createFile(newFile);
			
			YamlHeaderUpdater.saveMarkdownFileWithHeader(newFile, meta, "");
		} catch (Exception e) {
			log.error("", e);
			throw new RPCException(0, e.getMessage());
		}

		return result;
	}
}
