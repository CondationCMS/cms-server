package com.github.thmarx.cms.api.db.taxonomy;

/*-
 * #%L
 * cms-api
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


import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.Page;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author t.marx
 */
public interface Taxonomies {

	public List<Taxonomy> all ();
	
	public Optional<Taxonomy> forSlug (String slug);
	
	public Map<String, Integer> valueCount (Taxonomy taxonomy);
	
	public Set<String> values (Taxonomy taxonomy);
	
	public List<ContentNode> withValue (Taxonomy taxonomy, Object value);
	
	public Page<ContentNode> withValue (Taxonomy taxonomy, Object value, long page, long size);
}
