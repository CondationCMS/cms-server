package com.condation.cms.core.serivce.impl;

/*-
 * #%L
 * cms-core
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
import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.SiteConfiguration;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.eventbus.events.ReIndexContentMetaDataEvent;
import com.condation.cms.api.feature.features.EventBusFeature;
import com.condation.cms.api.utils.HTTPUtil;
import com.condation.cms.api.utils.PathUtil;
import com.condation.cms.core.content.ContentResolvingStrategy;
import com.condation.cms.core.content.io.ContentFileParser;
import com.condation.cms.core.content.io.YamlHeaderUpdater;
import com.condation.cms.core.serivce.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author thmar
 */
@Slf4j
public class NodeTranslationService implements Service {
	
	private final DB db;
	private final EventBus eventBus;
	
	public NodeTranslationService (final DB db, final EventBus eventBus) {
		this.db = db;
		this.eventBus = eventBus;
	}
	
	public boolean removeTranslation (String uri, String language) {
		var contentFile = ContentResolvingStrategy.resolve(uri, db).orElse(null);
		
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();
				var translations = (Map<String, Object>)meta.getOrDefault("translations", new HashMap<>());
				translations.remove(language);
				meta.put("translations", translations);
				
				var path = contentFile.relativePath();
				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(path);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, parser.getContent());
				log.debug("file {} saved", path);

				eventBus.publish(new ReIndexContentMetaDataEvent(path));
				
				return true;
			} catch (IOException ex) {
				log.error("", ex);
				return false;
			}
		}
		
		return false;
	}
	
	public boolean addTranslation (String uri, String site, String translationUri, String language) {

		var contentFile = ContentResolvingStrategy.resolve(uri, db).orElse(null);
		
		if (contentFile != null) {
			try {
				ContentFileParser parser = new ContentFileParser(contentFile);

				Map<String, Object> meta = parser.getHeader();
				var translations = (Map<String, Object>)meta.getOrDefault("translations", new HashMap<>());
				translations.put(language, PathUtil.toURL(translationUri));
				meta.put("translations", translations);
				
				var path = contentFile.relativePath();
				
				var filePath = db.getFileSystem().resolve(Constants.Folders.CONTENT).resolve(path);

				YamlHeaderUpdater.saveMarkdownFileWithHeader(filePath, meta, parser.getContent());
				log.debug("file {} saved", path);

				eventBus.publish(new ReIndexContentMetaDataEvent(path));
				
				return true;
			} catch (IOException ex) {
				log.error("", ex);
				return false;
			}
		}
		
		return false;
	}
}
