package com.github.thmarx.cms.modules.pug;

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

import com.github.thmarx.cms.api.ModuleFileSystem;
import com.github.thmarx.cms.api.ServerProperties;
import com.github.thmarx.cms.api.template.TemplateEngine;
import com.github.thmarx.cms.api.theme.Theme;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.template.FileTemplateLoader;
import de.neuland.pug4j.template.PugTemplate;
import de.neuland.pug4j.template.TemplateLoader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author thmar
 */
public class PugTemplateEngine implements TemplateEngine {

	private final PugConfiguration config;
	
	public PugTemplateEngine(final ModuleFileSystem fileSystem, final ServerProperties properties, Theme theme) {
		
		config = new PugConfiguration();
		config.setTemplateLoader(createTemplateLoader(fileSystem, theme));
		config.setCaching(false);
		
		if (properties.dev()) {
			config.setCaching(false);
		} else {
			config.setCaching(true);
		}
	}
	
	private TemplateLoader createTemplateLoader (final ModuleFileSystem fileSystem, Theme theme) {
		var templateBase = fileSystem.resolve("templates/");
		var siteTemplateLoader = new FileTemplateLoader(templateBase.toAbsolutePath().toString(), StandardCharsets.UTF_8);
		Optional<TemplateLoader> themeTemplateLoader;
		if (!theme.empty()) {
			themeTemplateLoader = Optional.of(
					new FileTemplateLoader(theme.templatesPath().toAbsolutePath().toString(), StandardCharsets.UTF_8));
		} else {
			themeTemplateLoader = Optional.empty();
		}
		
		return new ThemeTemplateLoader(siteTemplateLoader, themeTemplateLoader);
	}

	@Override
	public String render(String template, Model model) throws IOException {

		Writer writer = new StringWriter();

		PugTemplate compiledTemplate = config.getTemplate(template);

		config.renderTemplate(compiledTemplate, model.values, writer);

		return writer.toString();

	}

	@Override
	public void invalidateCache() {
		config.clearCache();
	}

}
