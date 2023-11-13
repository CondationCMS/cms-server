package com.github.thmarx.cms.modules.thymeleaf;

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
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 *
 * @author thmar
 */

public class ThymeleafTemplateEngine implements TemplateEngine {

	private final org.thymeleaf.TemplateEngine engine;
	private final ServerProperties serverProperties;

	public ThymeleafTemplateEngine(final ModuleFileSystem fileSystem, 
			final ServerProperties serverProperties,
			final Theme theme) {
	
		
		this.serverProperties = serverProperties;

		var templateBase = fileSystem.resolve("templates/");
		
		ITemplateResolver siteTemplateResolver = templateResolver(templateBase);
		ITemplateResolver themeTemplateResolver = null;
		if (!theme.empty()) {
			themeTemplateResolver = templateResolver(theme.templatesPath());
		}

		engine = new org.thymeleaf.TemplateEngine();
		engine.setTemplateResolver(new ThemeTemplateResolver(siteTemplateResolver, Optional.ofNullable(themeTemplateResolver)));
	}
	
	private ITemplateResolver templateResolver (final Path templatePath) {
		var templateResolver = new FileTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setPrefix(templatePath.toString() + File.separatorChar);
		//templateResolver.setSuffix(".html");
		if (serverProperties.dev()) {
			templateResolver.setCacheable(false);
		} else {
			templateResolver.setCacheable(true);
			templateResolver.setCacheTTLMs(TimeUnit.MINUTES.toMillis(1));
		}
		
		return templateResolver;
	}

	@Override
	public String render(String template, TemplateEngine.Model model) throws IOException {

		Writer writer = new StringWriter();
		engine.process(template, new Context(Locale.getDefault(), model.values), writer);
		return writer.toString();
	}

	@Override
	public void invalidateCache() {
		engine.getCacheManager().clearAllCaches();
	}

}
