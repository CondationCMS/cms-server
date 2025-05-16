package com.condation.cms.modules.ui.commands.content;

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
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.module.CMSModuleContext;
import com.condation.cms.api.module.CMSRequestContext;
import com.condation.cms.modules.ui.services.CommandService;
import com.condation.cms.modules.ui.utils.ContentFileParser;
import com.condation.cms.modules.ui.utils.YamlHeaderUpdater;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class AddSectionCommand {

	public static final String NAME = "addSection";

	public static final CommandService.CommandHandler getHandler(
			final CMSModuleContext moduleContext, final CMSRequestContext requestContext
	) {
		final DB db = moduleContext.get(DBFeature.class).db();
		var contentBase = db.getReadOnlyFileSystem().resolve(Constants.Folders.CONTENT);
		return command -> {
			var content = (String) command.parameters().getOrDefault("content", "");
			var uri = (String) command.parameters().get("uri");
			var template = (String) command.parameters().get("template");

			var contentFile = contentBase.resolve(uri);
			
			Map<String, Object> result = new HashMap<>();
			result.put("uri", uri);
			if (contentFile != null) {
				try {
					Map<String, Object> meta = Map.of("template", template);
					
					var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(uri);
					
					YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, content);
					log.debug("file {} saved", uri);
					
					moduleContext.get(EventBusFeature.class).eventBus().publish(new ReIndexContentMetaDataEvent(uri));
				} catch (IOException ex) {
					result.put("error", true);
					log.error("", ex);
				}
			}

			return result;
		};
	}
}
