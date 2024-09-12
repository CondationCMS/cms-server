package com.condation.cms.api.configuration.loader;

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
import com.condation.cms.api.configuration.Loader;
import com.condation.cms.api.configuration.configs.TaxonomyConfiguration;
import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
public class TaxonomyConfigurationLoader implements Loader<TaxonomyConfiguration> {

	private final Path hostBase;

	@Override
	public TaxonomyConfiguration load() throws IOException {

		ConcurrentMap<String, Taxonomy> taxonomies = new ConcurrentHashMap<>();

		var props = hostBase.resolve("config/taxonomy.yaml");
		if (Files.exists(props)) {
			Map<String, Object> data = new Yaml().load(Files.readString(props, StandardCharsets.UTF_8));

			var tasList = (List<Map>) data.getOrDefault("taxonomies", List.of());

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

		return new TaxonomyConfiguration(taxonomies);
	}

	@Override
	public void reload(TaxonomyConfiguration config) throws IOException {
		var tempConfig = load();
		config.getTaxonomies().clear();
		config.getTaxonomies().putAll(tempConfig.getTaxonomies());
	}

	private void loadValues(Taxonomy taxonomy) {
		try {
			var filename = "config/taxonomy.%s.yaml".formatted(taxonomy.getSlug());
			var filePath = hostBase.resolve(filename);
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

}
