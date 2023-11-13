package com.github.thmarx.cms.modules.pebble;

/*-
 * #%L
 * cms-server
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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Theme;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.cache.tag.CaffeineTagCache;
import io.pebbletemplates.pebble.cache.template.CaffeineTemplateCache;
import io.pebbletemplates.pebble.loader.DelegatingLoader;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thmar
 */
public class PebbleTemplateEngine implements TemplateEngine {

	private final PebbleEngine engine;
	
	
	public PebbleTemplateEngine(final ModuleFileSystem fileSystem, final ServerProperties properties, final Theme theme) {
		
		final PebbleEngine.Builder builder = new PebbleEngine.Builder()
				.loader(createLoader(fileSystem, theme));
		
		if (properties.dev()) {
			builder.templateCache(null);
			builder.tagCache(null);
			builder.cacheActive(false);
			builder.strictVariables(true);
		} else {
			var templateCache = new CaffeineTemplateCache(
					Caffeine.newBuilder()
							.expireAfterWrite(Duration.ofMinutes(1))
							.build()
			);
			builder.templateCache(templateCache);
			var tagCache = new CaffeineTagCache(
					Caffeine.newBuilder()
							.expireAfterWrite(Duration.ofMinutes(1))
							.build()
			);
			builder.tagCache(tagCache);
			builder.cacheActive(true);
		}
		
		engine = builder
				.build();
	}
	
	private Loader<?> createLoader (final ModuleFileSystem fileSystem, final Theme theme) {
		List<Loader<?>> loaders = new ArrayList<>();
		
		var siteLoader = new FileLoader();
		siteLoader.setPrefix(fileSystem.resolve("templates/").toString() + File.separatorChar);
		loaders.add(siteLoader);
		
		if (!theme.empty()) {
			var themeLoader = new FileLoader();
			themeLoader.setPrefix(theme.templatesPath().toString() + File.separatorChar);
			loaders.add(themeLoader);
		}
		
		return new DelegatingLoader(loaders);
	}

	@Override
	public String render(String template, Model model) throws IOException {

		Writer writer = new StringWriter();

		PebbleTemplate compiledTemplate = engine.getTemplate(template);

		compiledTemplate.evaluate(writer, model.values);

		return writer.toString();

	}

	@Override
	public void invalidateCache() {
		engine.getTemplateCache().invalidateAll();
	}

}
