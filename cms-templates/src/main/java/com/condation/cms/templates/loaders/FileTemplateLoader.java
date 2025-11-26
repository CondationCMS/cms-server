package com.condation.cms.templates.loaders;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import com.condation.cms.api.cache.ICache;
import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 *
 * @author t.marx
 */
public class FileTemplateLoader implements TemplateLoader {

	private final Path basePath;
	
	public FileTemplateLoader(Path basePath) {
		this.basePath = basePath;
	}
	
	@Override
	public String load(String template) {
		try {
			var path = basePath.resolve(template);
			
			// Sicherheitscheck: Verhindert Path Traversal
			if (!path.normalize().startsWith(basePath.normalize())) {
				throw new SecurityException("Invalid template path: " + template);
			}
			
			if (!Files.exists(path)) {
				throw new TemplateNotFoundException("Template not found: " + template);
			}
			
			String content = Files.readString(path);
			return content;
			
		} catch (IOException e) {
			throw new TemplateNotFoundException("Failed to load template: " + template, e);
		}
	}
}
