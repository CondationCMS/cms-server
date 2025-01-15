package com.condation.cms.core.configuration.configs;

/*-
 * #%L
 * tests
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

import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.GSONProvider;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.configuration.reload.NoReload;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.core.configuration.source.TomlConfigSource;
import com.condation.cms.core.configuration.source.YamlConfigSource;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class TaxonomyConfiguration extends AbstractConfiguration implements IConfiguration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;
	private final Path hostBase;

	private ConcurrentMap<String, Taxonomy> taxonomies = new ConcurrentHashMap<>();
	
	public TaxonomyConfiguration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		this.hostBase = builder.hostBase;
		reloadStrategy.register(this);
		
		reload();
	}

	@Override
	protected List<ConfigSource> getSources() {
		return sources;
	}

	@Override
	public String id () {
		return id;
	}
	
	public static Builder builder (EventBus eventBus) {
		return new Builder(eventBus);
	}
	
	public ConcurrentMap<String, Taxonomy> getTaxonomies () {
		return taxonomies;
	}

	@Override
	public void reload () {
		taxonomies.clear();
		sources.forEach(source -> {
			if (source.reload()) {
				eventBus.publish(new ConfigurationReloadEvent(id));				
			}
			
			var taxos = getList("taxonomies", Taxonomy.class);
			taxos.forEach(taxo -> {
				taxonomies.put(taxo.slug, taxo);
				loadValues(taxo);
			});
		});
	}
	
	private void loadValues(Taxonomy taxonomy) {
		try {
			var yamlFile = "config/taxonomy.%s.yaml".formatted(taxonomy.getSlug());
			var tomlFile = "config/taxonomy.%s.toml".formatted(taxonomy.getSlug());
			
			var valueSrc = List.of(
					YamlConfigSource.build(hostBase.resolve(yamlFile)),
					TomlConfigSource.build(hostBase.resolve(tomlFile))
			);
			
			var values = valueSrc.stream()
					.filter(ConfigSource::exists)
					.map(config -> config.getList("values"))
					.flatMap(List::stream)
					.map(item -> toJson(item))
					.map(item -> fromJson(item))
					.collect(Collectors.toMap(Value::getId, Function.identity()));
			taxonomy.setValues(values);
		} catch (IOException ex) {
			log.error("", ex);
		}
	}
	private String toJson(Object item) throws JsonSyntaxException {
		return GSONProvider.GSON.toJson(item);
	}
	
	private Value fromJson(String item) throws JsonSyntaxException {
		return GSONProvider.GSON.fromJson(item, Value.class);
	}
	
	public static class Builder {
		private final List<ConfigSource> sources = new ArrayList<>();
		private ReloadStrategy reloadStrategy = new NoReload();
		private String id = UUID.randomUUID().toString();
		private final EventBus eventBus;
		private Path hostBase;
		
		public Builder (EventBus eventbus) {
			this.eventBus = eventbus;
		}
		
		public Builder hostBase (Path hostBase) {
			this.hostBase = hostBase;
			return this;
		}

		public Builder id (String uniqueId) {
			this.id = uniqueId;
			return this;
		}
		
		public Builder addSource(ConfigSource source) {
			sources.add(source);
			return this;
		}
		
		public Builder reloadStrategy (ReloadStrategy reload) {
			this.reloadStrategy = reload;
			return this;
		}
		
		public TaxonomyConfiguration build () {
			return new TaxonomyConfiguration(this);
		}
	}
}
