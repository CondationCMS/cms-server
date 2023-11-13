package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
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

import de.neuland.pug4j.template.TemplateLoader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class ThemeTemplateLoader implements TemplateLoader {

	final TemplateLoader siteTemplateLoader;
	final Optional<TemplateLoader> themeTemplateLoader;
	
	private String extension = "pug";

	@Override
	public long getLastModified(String name) throws IOException {
		if (themeTemplateLoader.isEmpty()) {
			return siteTemplateLoader.getLastModified(name);
		}
		try {
			return siteTemplateLoader.getLastModified(name);
		} catch (IOException nfe) {}
		return themeTemplateLoader.get().getLastModified(name);
	}

	@Override
	public Reader getReader(String name) throws IOException {
		if (themeTemplateLoader.isEmpty()) {
			return siteTemplateLoader.getReader(name);
		}
		try {
			return siteTemplateLoader.getReader(name);
		} catch (IOException nfe) {}
		return themeTemplateLoader.get().getReader(name);
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	public String getBase() {
		return "";
	}
	
	
	
}
