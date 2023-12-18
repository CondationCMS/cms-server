package com.github.thmarx.cms.filesystem.taxonomy;

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
import com.github.thmarx.cms.api.Constants;
import com.github.thmarx.cms.api.SiteProperties;
import com.github.thmarx.cms.api.db.ContentNode;
import com.github.thmarx.cms.api.db.Page;
import com.github.thmarx.cms.api.db.taxonomy.Taxonomies;
import com.github.thmarx.cms.api.db.taxonomy.Taxonomy;
import com.github.thmarx.cms.api.db.taxonomy.Value;
import com.github.thmarx.cms.api.eventbus.EventListener;
import com.github.thmarx.cms.api.eventbus.events.SitePropertiesChanged;
import com.github.thmarx.cms.api.utils.MapUtil;
import com.github.thmarx.cms.filesystem.FileSystem;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author t.marx
 */
@Slf4j
@RequiredArgsConstructor
public class FileTaxonomies implements Taxonomies, EventListener<SitePropertiesChanged> {

	private final SiteProperties siteProperties;
	private final FileSystem fileSystem;

	private ConcurrentMap<String, Taxonomy> taxonomies = new ConcurrentHashMap<>();


	public void reloadTaxonomies() {
		var tasList = (List<Map>) siteProperties.getOrDefault("taxonomy", Map.of()).getOrDefault("taxonomies", List.of());

		tasList.stream().map((taxo) -> {
			Taxonomy tax = new Taxonomy();
			tax.setTitle((String) taxo.get("title"));
			tax.setSlug((String) taxo.get("slug"));
			tax.setField((String) taxo.get("field"));
			tax.setTemplate((String) taxo.getOrDefault("template", Constants.Taxonomy.DEFAULT_TEMPLATE));
			tax.setSingleTemplate((String) taxo.getOrDefault("template_single", Constants.Taxonomy.DEFAULT_SINGLE_TEMPLATE));
			tax.setArray((Boolean) taxo.getOrDefault("array", false));
			
			loadValues(tax);
			
			return tax;
		}).forEach(tax -> taxonomies.put(tax.getSlug(), tax));
	}
	
	private void loadValues (Taxonomy taxonomy) {		
		try {
			var filename = "config/taxonomy.%s.yaml".formatted(taxonomy.getSlug());
			var filePath = fileSystem.resolve(filename);
			if (Files.exists(filePath)) {
				Map<String, Object> taxoConfig = new Yaml().load(Files.readString(filePath, StandardCharsets.UTF_8));
				List<Map<String, Object>> values = (List<Map<String, Object>>) taxoConfig.getOrDefault("values", List.of());
				values.forEach((valueMap) -> {
					String title = (String) valueMap.get("title");
					String id = (String) valueMap.get("id");
					if (!Strings.isNullOrEmpty(id) && !Strings.isNullOrEmpty(title)) {
						var val = new Value(id, title);
						taxonomy.getValues().put(id, val);
					}
				});
			}
		} catch (IOException ioe) {
			log.error(null, ioe);
		}
	}

	@Override
	public List<Taxonomy> all() {
		return new ArrayList<>(taxonomies.values());
	}
	
	@Override
	public Optional<Taxonomy> forSlug(final String slug) {
		return Optional.ofNullable(taxonomies.get(slug));
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
		List<ContentNode> nodes = null;
		if (taxonomy.isArray()) {
			nodes = fileSystem.query((node, index) -> node).whereContains(taxonomy.getField(), value).get();
		} else {
			nodes = fileSystem.query((node, index) -> node).where(taxonomy.getField(), value).get();
		}

		return nodes;
	}
	
	@Override
	public Page<ContentNode> withValue(Taxonomy taxonomy, Object value, long page, long size) {
		
		if (taxonomy.isArray()) {
			return fileSystem.query((node, index) -> node).whereContains(taxonomy.getField(), value)
					.page(page, size);
		} else {
			return fileSystem.query((node, index) -> node).where(taxonomy.getField(), value)
					.page(page, size);
		}
	}
	
	@Override
	public void consum(SitePropertiesChanged event) {
		reloadTaxonomies();
	}
}
