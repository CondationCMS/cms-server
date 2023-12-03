package com.github.thmarx.cms.modules.search.extension;

/*-
 * #%L
 * thymeleaf-module
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
import com.github.thmarx.cms.api.CMSModuleContext;
import com.github.thmarx.cms.api.eventbus.events.ContentChangedEvent;
import com.github.thmarx.cms.modules.search.SearchEngine;
import com.github.thmarx.modules.api.ModuleLifeCycleExtension;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@Extension(ModuleLifeCycleExtension.class)
public class SearchLifecycleExtension extends ModuleLifeCycleExtension<CMSModuleContext> {

	static SearchEngine searchEngine;

	@Override
	public void init() {
	}

	@Override
	public void activate() {
		searchEngine = new SearchEngine();
		try {
			searchEngine.open(configuration.getDataDir().toPath().resolve("index"), getContext().getSiteProperties().getOrDefault("language", "standard"));

			// stat reindexing
			Thread.ofVirtual().start(() -> {

				reindexContext();
			});
		} catch (IOException e) {
			log.error("error opening serach engine", e);
			throw new RuntimeException(e);
		}
		
		getContext().getEventBus().register(ContentChangedEvent.class, (event) -> {
			reindexContext();
		});
	}

	protected void reindexContext() {
		var contentPath = getContext().getDb().getFileSystem().resolve("content");
		try {
			searchEngine.clear();
			Files.walkFileTree(contentPath, new FileIndexingVisitor(
					contentPath, 
					SearchLifecycleExtension.searchEngine, 
					getContext()
			));
			searchEngine.commit();
		} catch (IOException e) {
			log.error(null, e);
		}
	}

	@Override
	public void deactivate() {
		try {
			searchEngine.close();
		} catch (Exception e) {
			log.error("error closing serach engine", e);
			throw new RuntimeException(e);
		}
	}
}
