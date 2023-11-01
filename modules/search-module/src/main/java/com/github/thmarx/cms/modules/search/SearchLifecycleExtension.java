package com.github.thmarx.cms.modules.search;

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
import com.github.thmarx.modules.api.ModuleLifeCycleExtension;
import com.github.thmarx.modules.api.annotation.Extension;
import java.io.IOException;
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
		searchEngine = new SearchEngine(configuration.getDataDir().toPath().resolve("index"), getContext().getSiteProperties().getOrDefault("language", "standard"));
		try {
			searchEngine.open();
			
			// stat reindexing
			Thread.ofVirtual().start(() -> {
			});
		} catch (IOException e) {
			log.error("error opening serach engine", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void deactivate() {
		try {
			searchEngine.close();
		} catch (IOException e) {
			log.error("error closing serach engine", e);
			throw new RuntimeException(e);
		}
	}
}
