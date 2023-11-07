package com.github.thmarx.cms.modules.search.extension;

/*-
 * #%L
 * thymeleaf-module
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
		var contentPath = getContext().getFileSystem().resolve("content");
		try {
			searchEngine.clear();
			Files.walkFileTree(contentPath, new FileIndexingVisitor(contentPath, SearchLifecycleExtension.searchEngine, getContext()));
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
