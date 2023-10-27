package com.github.thmarx.cms.modules.freemarker;

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
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.NullCacheStorage;
import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerTemplateEngine implements TemplateEngine {

	private final Configuration config;


	final ModuleFileSystem fileSystem;

	public FreemarkerTemplateEngine(final ModuleFileSystem fileSystem, final ServerProperties serverProperties) {
		this.fileSystem = fileSystem;

		config = new Configuration(Configuration.VERSION_2_3_32);

		try {
			config.setTemplateLoader(new FileTemplateLoader(fileSystem.resolve("templates/").toFile()));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		config.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
		config.setDefaultEncoding("UTF-8");
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		config.setLogTemplateExceptions(false);
		config.setWrapUncheckedExceptions(true);
		config.setFallbackOnNullLoopVariable(false);

		if (serverProperties.dev()) {
			config.setCacheStorage(new NullCacheStorage());
			config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		} else {
			config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			config.setLogTemplateExceptions(false);
			config.setWrapUncheckedExceptions(true);
			config.setFallbackOnNullLoopVariable(false);
		}

		config.setSharedVariable("indexOf", new IndexOfMethod());
		config.setSharedVariable("upper", new UpperDirective());
	}

	@Override
	public String render(final String template, final FreemarkerTemplateEngine.Model model) throws IOException {
		StringWriter out = new StringWriter();
		try {
			Template loadedTemplate = config.getTemplate(template);

			loadedTemplate.process(model.values, out);

			return out.toString();
		} catch (TemplateException | IOException e) {
			throw new IOException(e);
		} finally {
			out.close();
		}
	}

	@Override
	public void invalidateCache() {
		config.clearTemplateCache();
	}

}
