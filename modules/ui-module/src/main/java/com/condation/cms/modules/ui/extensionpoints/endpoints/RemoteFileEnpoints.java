package com.condation.cms.modules.ui.extensionpoints.endpoints;

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

import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.cms.ReadOnlyFile;
import com.condation.cms.api.db.cms.ReadyOnlyFileSystem;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.ui.annotations.RemoteEndpoint;
import com.condation.cms.api.ui.extensions.UIRemoteEndpointExtensionPoint;
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

/**
 *
 * @author thorstenmarx
 */
@Slf4j
@Extension(UIRemoteEndpointExtensionPoint.class)
public class RemoteFileEnpoints extends UIRemoteEndpointExtensionPoint {

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

	@RemoteEndpoint(endpoint = "files.list")
	public Object list(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();
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
					files.add(new File("..", parent.uri(), parent.isDirectory()));
				}
				contentFile.children().stream()
						.filter(child -> !SectionUtil.isSection(child.getFileName()))
						.map(child -> new File(
						child.getFileName(),
						child.uri(),
						child.isDirectory()
				)).forEach(files::add);
			} catch (IOException ex) {
				log.error("", ex);
			}
		}
		result.put("files", files);

		return result;
	}

	@RemoteEndpoint(endpoint = "files.delete")
	public Object delete(Map<String, Object> parameters) {
		final DB db = getContext().get(DBFeature.class).db();

		Map<String, Object> result = new HashMap<>();

		try {
			var uri = (String) parameters.getOrDefault("uri", "");
			var type = (String) parameters.get("type");
			var contentBase = getBase(db.getReadOnlyFileSystem(), type);

			var contentFile = contentBase.resolve(uri);

			log.debug("deleting file {}", contentFile.uri());
			if (contentFile.isDirectory()) {
				FileUtils.deleteFolder(db.getFileSystem().resolve(uri));
			} else if ("assets".equals(type)) {
				Files.deleteIfExists(db.getFileSystem().resolve(uri));
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
			result.put("error", true);
		}

		return result;
	}

	public record File(String name, String uri, boolean directory) {

		public File(String name, String uri) {
			this(name, uri, false);
		}
	}
}
