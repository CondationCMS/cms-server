package com.condation.cms.filesystem.taxonomy;

/*-
 * #%L
 * cms-filesystem
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

import com.condation.cms.api.configuration.Configuration;
import com.condation.cms.api.configuration.configs.TaxonomyConfiguration;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.db.taxonomy.Taxonomies;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.filesystem.FileSystem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class FileTaxonomies implements Taxonomies {

	private final Configuration configuration;
	private final FileSystem fileSystem;

	@Override
	public List<Taxonomy> all() {
		return new ArrayList<>(configuration.get(TaxonomyConfiguration.class).getTaxonomies().values());
	}
	
	@Override
	public Optional<Taxonomy> forSlug(final String slug) {
		return Optional.ofNullable(configuration.get(TaxonomyConfiguration.class).getTaxonomies().get(slug));
	}

	@Override
	public Map<String, Integer> valueCount(Taxonomy taxonomy) {
		fileSystem.query((node, index) -> node).where(taxonomy.getField(), "!=", null);
		return Map.of();
	}

	@Override
	public Set<String> values(Taxonomy taxonomy) {
		var nodes = fileSystem.query((node, index) -> node).where(taxonomy.getField(), "!=", null).get();

		Set<String> values = new HashSet<>();
		nodes.forEach(node -> {
			var value = MapUtil.getValue(node.data(), taxonomy.getField());
			if (value instanceof List) {
				values.addAll((List) value);
			} else {
				values.add((String) value);
			}
		});

		return values;
	}

	@Override
	public List<ContentNode> withValue(final Taxonomy taxonomy, final Object value) {
		List<ContentNode> nodes;
		if (taxonomy.isArray()) {
			nodes = fileSystem
					.query((node, index) -> node).whereContains(taxonomy.getField(), value)
					.orderby("title").asc()
					.get();
		} else {
			nodes = fileSystem
					.query((node, index) -> node)
					.where(taxonomy.getField(), value)
					.orderby("title").asc()
					.get();
		}

		return nodes;
	}
	
	@Override
	public Page<ContentNode> withValue(Taxonomy taxonomy, Object value, long page, long size) {
		
		if (taxonomy.isArray()) {
			return fileSystem.query((node, index) -> node)
					.whereContains(taxonomy.getField(), value)
					.orderby("title").asc()
					.page(page, size);
		} else {
			return fileSystem.query((node, index) -> node)
					.where(taxonomy.getField(), value)
					.orderby("title").asc()
					.page(page, size);
		}
	}
}
