package com.github.thmarx.cms.filesystem.functions.taxonomy;

/*-
 * #%L
 * cms-filesystem
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

import com.github.thmarx.cms.api.db.taxonomy.Taxonomy;
import com.github.thmarx.cms.filesystem.FileDB;
import com.google.inject.Inject;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class TaxonomyFunction {
	
	private final FileDB fileDB;
	
	public List<Taxonomy> taxonomies () {
		return fileDB.getTaxonomies().all();
	}
	
	public String url (final String taxonomy, final String value) {
		return "/%s/%s".formatted(taxonomy, value);
	}
	
	public String getTitle (final String taxonomy) {
		
		var taxo = fileDB.getTaxonomies().forSlug(taxonomy);
		if (taxo.isPresent()) {
			return taxo.get().getTitle();
		}
		
		return taxonomy;
	}
	
	public String getTitle (final String taxonomy, String value) {
		
		var taxo = fileDB.getTaxonomies().forSlug(taxonomy);
		if (taxo.isPresent()) {
			
			return taxo.get().getValueTitle(value);
		}
		
		return taxonomy;
	}
}
