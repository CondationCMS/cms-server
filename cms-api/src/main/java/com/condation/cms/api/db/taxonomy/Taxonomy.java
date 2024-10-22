package com.condation.cms.api.db.taxonomy;

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


import com.condation.cms.api.Constants;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author t.marx
 */
@NoArgsConstructor
@Data
public class Taxonomy {
	public String title;
	public String slug;
	public String template = Constants.Taxonomy.DEFAULT_TEMPLATE;
	public String singleTemplate = Constants.Taxonomy.DEFAULT_SINGLE_TEMPLATE;
	public String field;
	public boolean array = false;
	public Map<String, Value> values = new HashMap<>();

	public Taxonomy(String title, String slug, String field) {
		this.title = title;
		this.slug = slug;
		this.field = field;
	}
	
	public String getValueTitle (final String value) {
		if (values.containsKey(value)) {
			return values.get(value).getTitle();
		}
		return value;
	}
}
