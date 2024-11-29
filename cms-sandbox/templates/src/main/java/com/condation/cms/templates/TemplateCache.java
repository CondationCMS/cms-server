package com.condation.cms.templates;

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
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class TemplateCache {
	
	private final ICache<String, Template> templates;
	
	protected void invalidate () {
		templates.invalidate();
	}
	
	public Optional<Template> get (String key) {
		return Optional.ofNullable(templates.get(key));
	}
	
	public boolean contains (String key) {
		return templates.contains(key);
	}
	
	public void put (String key, Template template) {
		templates.put(key, template);
	}
}
