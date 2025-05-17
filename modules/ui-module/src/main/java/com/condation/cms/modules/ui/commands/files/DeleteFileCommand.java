package com.condation.cms.modules.ui.commands.files;

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
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.module.CMSRequestContext;
import com.condation.cms.api.utils.FileUtils;
import com.condation.cms.modules.ui.services.CommandService;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class DeleteFileCommand {

	public static final String NAME = "deleteFile";

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

	public static final CommandService.CommandHandler getHandler(
			final CMSModuleContext moduleContext, final CMSRequestContext requestContext
	) {
		final DB db = moduleContext.get(DBFeature.class).db();

		return command -> {

			Map<String, Object> result = new HashMap<>();

			try {
				var uri = (String) command.parameters().getOrDefault("uri", "");
				var type = (String) command.parameters().get("type");
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
		};
	}
}
