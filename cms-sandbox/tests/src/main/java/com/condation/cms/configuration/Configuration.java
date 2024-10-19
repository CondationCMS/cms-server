package com.condation.cms.configuration;

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.configuration.reload.NoReload;
import com.condation.cms.configuration.reload.ReloadEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class Configuration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;

	private final Gson GSON;
	
	public Configuration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		reloadStrategy.register(this);
		
		GSON = new GsonBuilder()
				.enableComplexMapKeySerialization()
				.create();
	}

	public String id () {
		return id;
	}
	
	public static Builder builder (EventBus eventBus) {
		return new Builder(eventBus);
	}

	public void reload () {
		sources.forEach(source -> {
			if (source.reload()) {
				eventBus.publish(new ReloadEvent(id));
			}
		});
	}
	
	private <T> T getValue (String field, Class<T> typeClass) {
		var value = sources.reversed().stream()
				.filter(ConfigSource::exists)
				.map(config -> config.get(field))
				.filter(cv -> cv != null)
				.filter(typeClass::isInstance)
				.map(typeClass::cast)
				.findFirst();
		return value.isPresent() ? value.get() : null;
	}
	
	public String getString (String field) {
		return getValue(field, String.class);
	}
	
	public Integer getInteger (String field) {
		return getValue(field, Integer.class);
	}
	public Double getDouble (String field) {
		return getValue(field, Double.class);
	}
	public Float getFloat (String field) {
		return getValue(field, Float.class);
	}
	public Long getLong (String field) {
		return getValue(field, Long.class);
	}

	public Map<String, Object> get (String field) {
		Map<String, Object> result = new HashMap<>();
		sources.stream()
				.filter(ConfigSource::exists)
				.map(config -> config.getMap(field))
				.forEach(sourceMap -> {
					MapUtil.deepMerge(result, sourceMap);
				});
		return result;
	}
	
	public List<Object> getList (String field) {
		List<Object> result = new ArrayList<>();
		sources.stream()
				.filter(ConfigSource::exists)
				.map(config -> config.getList(field))
				.forEach(result::addAll);
		return result;
	}
	
	public <T> List<T> getList(String field, Class<T> aClass) {
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
	
	public <T> T get(String field, Class<T> aClass) {
		try {
			var map = get(field);
			var json_string = GSON.toJson(map);
			
			return GSON.fromJson(json_string, aClass);
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
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
		
		public Configuration build () {
			return new Configuration(this);
		}
	}
}
