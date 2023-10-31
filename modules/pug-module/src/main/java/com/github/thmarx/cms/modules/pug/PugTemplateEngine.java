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
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.expression.GraalJsExpressionHandler;
import de.neuland.pug4j.template.FileTemplateLoader;
import de.neuland.pug4j.template.PugTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 *
 * @author thmar
 */
public class PugTemplateEngine implements TemplateEngine {

	private final PugConfiguration config;
	private final Path templateBase;
	
	public PugTemplateEngine(final ModuleFileSystem fileSystem, final ServerProperties properties) {
		this.templateBase = fileSystem.resolve("templates/");
		
		config = new PugConfiguration();
		var fileTemplateLoader = new FileTemplateLoader(templateBase.toAbsolutePath().toString(), StandardCharsets.UTF_8);
		config.setTemplateLoader(fileTemplateLoader);
		config.setCaching(false);
		//config.setExpressionHandler(new GraalJsExpressionHandler());
		
		if (properties.dev()) {
			config.setCaching(false);
		} else {
			config.setCaching(true);
		}
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
