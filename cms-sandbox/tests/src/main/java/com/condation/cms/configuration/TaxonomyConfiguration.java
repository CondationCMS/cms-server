package com.condation.cms.configuration;

import com.condation.cms.api.db.taxonomy.Taxonomy;
import com.condation.cms.api.db.taxonomy.Value;
import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.configuration.reload.NoReload;
import com.condation.cms.configuration.reload.ReloadEvent;
import com.condation.cms.configuration.source.TomlConfigSource;
import com.condation.cms.configuration.source.YamlConfigSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class TaxonomyConfiguration implements IConfiguration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;

	private final Gson GSON;
	
	private ConcurrentMap<String, Taxonomy> taxonomies = new ConcurrentHashMap<>();
	
	public TaxonomyConfiguration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		reloadStrategy.register(this);
		
		GSON = new GsonBuilder()
				.enableComplexMapKeySerialization()
				.create();
		
		reload();
	}

	@Override
	public String id () {
		return id;
	}
	
	public static Builder builder (EventBus eventBus) {
		return new Builder(eventBus);
	}
	
	public Map<String, Taxonomy> getTaxonomies () {
		return taxonomies;
	}

	@Override
	public void reload () {
		taxonomies.clear();
		sources.forEach(source -> {
			if (source.reload()) {
				eventBus.publish(new ReloadEvent(id));				
			}
			
			var taxos = getList("taxonomies", Taxonomy.class);
			taxos.forEach(taxo -> {
				taxonomies.put(taxo.slug, taxo);
				loadValues(taxo);
			});
		});
	}
	
	private List<Object> getList (String field) {
		List<Object> result = new ArrayList<>();
		sources.stream()
				.filter(ConfigSource::exists)
				.map(config -> config.getList(field))
				.forEach(result::addAll);
		return result;
	}
	
	private <T> List<T> getList(String field, Class<T> aClass) {
		try {
			var list = getList(field);
			
			return list.stream()
					.map(item -> GSON.toJson(item))
					.map(item -> GSON.fromJson(item, aClass))
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	private void loadValues(Taxonomy taxonomy) {
		try {
			var yamlFile = "configs/taxonomy.%s.yaml".formatted(taxonomy.getSlug());
			var tomlFile = "configs/taxonomy.%s.toml".formatted(taxonomy.getSlug());
			
			var valueSrc = List.of(
					YamlConfigSource.build(Path.of(yamlFile)),
					TomlConfigSource.build(Path.of(tomlFile))
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
		return GSON.toJson(item);
	}
	
	private Value fromJson(String item) throws JsonSyntaxException {
		return GSON.fromJson(item, Value.class);
	}
	
	public static class Builder {
		private final List<ConfigSource> sources = new ArrayList<>();
		private ReloadStrategy reloadStrategy = new NoReload();
		private String id = UUID.randomUUID().toString();
		private final EventBus eventBus;
		
		public Builder (EventBus eventbus) {
			this.eventBus = eventbus;
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
