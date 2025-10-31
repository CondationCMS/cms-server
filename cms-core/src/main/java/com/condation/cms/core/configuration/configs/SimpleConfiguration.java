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

import com.condation.cms.api.eventbus.EventBus;
import com.condation.cms.api.utils.MapUtil;
import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.GSONProvider;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.configuration.reload.NoReload;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class SimpleConfiguration extends AbstractConfiguration implements IConfiguration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;
	
	public SimpleConfiguration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		reloadStrategy.register(this);
	}

	@Override
	public List<ConfigSource> getSources() {
		return sources;
	}

	@Override
	public String id () {
		return id;
	}
	
	public static Builder builder (EventBus eventBus) {
		return new Builder(eventBus);
	}
	public static Builder builder () {
		return new Builder(null);
	}

	@Override
	public void reload () {
		sources.forEach(source -> {
			if (source.reload() && eventBus != null) {
				eventBus.publish(new ConfigurationReloadEvent(id));
			}
		});
	}
	
	public Object get (String field) {
		var value = sources.reversed().stream()
				.filter(ConfigSource::exists)
				.map(config -> config.get(field))
				.filter(cv -> cv != null)
				.findFirst();
		return value.isPresent() ? value.get() : null;
	}
	
	public Object getOrDefault (String field, Object defaultValue) {
		var value = sources.reversed().stream()
				.filter(ConfigSource::exists)
				.map(config -> config.get(field))
				.filter(cv -> cv != null)
				.findFirst();
		return value.isPresent() ? value.get() : defaultValue;
	}
	
	public <T> T getValue (String field, Class<T> typeClass) {
		var value = sources.reversed().stream()
				.filter(ConfigSource::exists)
				.map(config -> config.get(field))
				.filter(cv -> cv != null)
				.filter(typeClass::isInstance)
				.map(typeClass::cast)
				.findFirst();
		return value.isPresent() ? value.get() : null;
	}
	
	public Boolean getBooleam (String field) {
		return getValue(field, Boolean.class);
	}
	public Boolean getBoolean (String field, boolean defaultValue) {
		var value = getValue(field, Boolean.class);
		return value != null ? value : defaultValue;
	}
	
	public String getString (String field) {
		return getValue(field, String.class);
	}
	public String getString (String field, String defaultValue) {
		String value = getValue(field, String.class);
		return value != null ? value : defaultValue;
	}
	
	public Integer getInteger (String field, int defaultValue) {
		var value = getValue(field, Integer.class);
		
		return value != null ? value : defaultValue;
	}
	public Double getDouble (String field) {
		return getValue(field, Double.class);
	}
	public Double getDouble (String field, double defaultValue) {
		var value = getValue(field, Double.class);
		return value != null ? value : defaultValue;
	}
	public Float getFloat (String field) {
		return getValue(field, Float.class);
	}
	public Float getFloat (String field, float defaultValue) {
		var value = getValue(field, Float.class);
		return value != null ? value : defaultValue;
	}
	public Long getLong (String field) {
		return getValue(field, Long.class);
	}
	public Long getLong (String field, long defaultValue) {
		var value = getValue(field, Long.class);
		return value != null ? value : defaultValue;
	}

	public Map<String, Object> getMap (String field) {
		Map<String, Object> result = new HashMap<>();
		sources.stream()
				.filter(ConfigSource::exists)
				.map(config -> config.getMap(field))
				.forEach(sourceMap -> {
					MapUtil.deepMerge(result, sourceMap);
				});
		return result;
	}
	
	public <T> T get(String field, Class<T> aClass) {
		try {
			var map = getMap(field);
			var json_string = GSONProvider.GSON.toJson(map);
			
			return GSONProvider.GSON.fromJson(json_string, aClass);
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
		
		public SimpleConfiguration build () {
			return new SimpleConfiguration(this);
		}
	}
}