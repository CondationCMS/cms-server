package com.github.thmarx.cms.modules.pug;

/*-
 * #%L
 * pug-module
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
