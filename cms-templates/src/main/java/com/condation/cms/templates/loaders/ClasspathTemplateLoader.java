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
 * You should have received a copy of the GNU General
 * Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Lädt Templates aus dem Klassenpfad. z. B. für Ressourcen im Ordner /templates
 * innerhalb des JARs.
 *
 * @author t.marx
 */
public class ClasspathTemplateLoader implements TemplateLoader {

	private final String basePath;

	public ClasspathTemplateLoader(String basePath) {
		this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
	}

	@Override
	public String load(String template) {
		var resourcePath = basePath + template;
		try (var resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);) {
			if (resourceStream == null) {
				throw new TemplateNotFoundException("Template not found in classpath: " + resourcePath);
			}

			try (var reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
				return reader.lines().collect(Collectors.joining("\n"));
			} catch (Exception e) {
				throw new TemplateNotFoundException("Error loading template from classpath: " + e.getMessage());
			}
		} catch (IOException ex) {
			throw new TemplateNotFoundException("Error loading template from classpath: " + ex.getMessage());
		}
	}
}
