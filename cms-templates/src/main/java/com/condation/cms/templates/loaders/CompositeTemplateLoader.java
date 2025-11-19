package com.condation.cms.templates.loaders;

/*-
 * #%L
 * cms-templates
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

import com.condation.cms.templates.TemplateLoader;
import com.condation.cms.templates.exceptions.TemplateNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class CompositeTemplateLoader implements TemplateLoader {

	private final List<TemplateLoader> templateLoaders;
	
	@Override
	public String load(String template) {
		for (var templateLoader : templateLoaders) {
			try {
				String content = templateLoader.load(template);
				
				return content;
			} catch (TemplateNotFoundException tnfe) {
				// nothing to do here, try next template loader
			}
		}
		throw new TemplateNotFoundException("template %s not found".formatted(template));
	}

	@Override
	public void invalidate() {
		this.templateLoaders.forEach(TemplateLoader::invalidate);
	}
	
	
}
