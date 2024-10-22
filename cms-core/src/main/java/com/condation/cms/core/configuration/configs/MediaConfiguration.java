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
import com.condation.cms.core.configuration.ConfigSource;
import com.condation.cms.core.configuration.GSONProvider;
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.configuration.reload.NoReload;
import com.condation.cms.core.configuration.reload.ReloadEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author t.marx
 */
@Slf4j
public class MediaConfiguration extends AbstractConfiguration implements IConfiguration {

	private final List<ConfigSource> sources;
	private final ReloadStrategy reloadStrategy;
	private final EventBus eventBus;
	private final String id;

	public MediaConfiguration(Builder builder) {
		this.sources = builder.sources;
		this.reloadStrategy = builder.reloadStrategy;
		this.eventBus = builder.eventBus;
		this.id = builder.id;
		reloadStrategy.register(this);
		
		reload();
	}

	@Override
	public List<ConfigSource> getSources() {
		return sources;
	}
	
	@Override
	public String id () {
		return id;
	}

	@Override
	public void reload () {
		sources.forEach(source -> {
			if (source.reload()) {
				eventBus.publish(new ReloadEvent(id));				
			}
		});
	}
	
	
	public List<Format> getFormats () {
		return getList("formats", Format.class);
	}
	
	public static MediaConfiguration.Builder builder (EventBus eventBus) {
		return new MediaConfiguration.Builder(eventBus);
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
		
		public MediaConfiguration build () {
			return new MediaConfiguration(this);
		}
	}
	
	@Data
	@NoArgsConstructor
	public static class MediaFormats {
		private List<Format> formats;
	}
	
	@Data
	@NoArgsConstructor
	public static class Format {
		private String name;
		private String format;
		private boolean compression;
		private int width;
		private int height;
	}
}
