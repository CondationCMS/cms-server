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
import com.condation.cms.api.utils.SectionUtil;
import com.condation.cms.modules.ui.services.CommandService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class ListFilesCommand {

	public static final String NAME = "listFiles";

	private static ReadOnlyFile getBase (ReadyOnlyFileSystem fileSystem, String type) {
		return switch (type) {
			case "content" -> fileSystem.contentBase();
			case "assets" -> fileSystem.assetBase();
			default -> null;
		};
	}
	
	public static final CommandService.CommandHandler getHandler(
			final CMSModuleContext moduleContext, final CMSRequestContext requestContext
	) {
		final DB db = moduleContext.get(DBFeature.class).db();
		
		return command -> {
			var uri = (String) command.parameters().getOrDefault("uri", "");
			if (uri == null) {
				uri = "";
			}
			if (uri.startsWith("/")){
				uri = uri.substring(1);
			}
			var type = (String) command.parameters().get("type");
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
		};
	}
	
	public record File (String name, String uri, boolean directory){
		public File (String name, String uri) {
			this(name, uri, false);
		}
	}
}
