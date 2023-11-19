package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * cms-server
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
