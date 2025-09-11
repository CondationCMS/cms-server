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
import com.condation.cms.core.configuration.IConfiguration;
import com.condation.cms.core.configuration.ReloadStrategy;
import com.condation.cms.core.configuration.reload.NoReload;
import com.condation.cms.api.eventbus.events.ConfigurationReloadEvent;
import com.condation.cms.api.media.MediaFormat;
import com.condation.cms.api.media.MediaUtils;
import com.google.common.hash.HashCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
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
	public String id() {
		return id;
	}

	@Override
	public void reload() {
		sources.forEach(source -> {
			if (source.reload()) {
				eventBus.publish(new ConfigurationReloadEvent(id));
			}
		});
	}

	public List<Format> getFormats() {
		var sorted = new TreeSet<Format>((o1, o2) -> o1.name.compareTo(o2.name));
		sorted.addAll(getList("formats", Format.class));
		return new ArrayList<>(sorted);
	}

	public List<MediaFormat> getMediaFormats() {
		return getFormats().stream().map(format -> {
			return new MediaFormat(format.name, format.width, format.height, toFormat(format.format), format.compression, format.cropped);
		}).toList();
	}

	private MediaUtils.Format toFormat(String format) {
		return switch (format) {
			case "png" ->
				MediaUtils.Format.PNG;
			case "webp" ->
				MediaUtils.Format.WEBP;
			case "jpeg" ->
				MediaUtils.Format.JPEG;
			default ->
				throw new AssertionError();
		};
	}

	public static MediaConfiguration.Builder builder(EventBus eventBus) {
		return new MediaConfiguration.Builder(eventBus);
	}

	public static class Builder {

		private final List<ConfigSource> sources = new ArrayList<>();
		private ReloadStrategy reloadStrategy = new NoReload();
		private String id = UUID.randomUUID().toString();
		private final EventBus eventBus;

		public Builder(EventBus eventbus) {
			this.eventBus = eventbus;
		}

		public Builder id(String uniqueId) {
			this.id = uniqueId;
			return this;
		}

		public Builder addSource(ConfigSource source) {
			sources.add(source);
			return this;
		}

		public Builder addAllSources(List<ConfigSource> sources) {
			this.sources.addAll(sources);
			return this;
		}

		public Builder reloadStrategy(ReloadStrategy reload) {
			this.reloadStrategy = reload;
			return this;
		}

		public MediaConfiguration build() {
			return new MediaConfiguration(this);
		}
	}

	@Data
	@NoArgsConstructor
	public static class Format {

		private String name;
		private String format;
		private boolean compression;
		private int width;
		private int height;
		private boolean cropped;

		@Override
		public final int hashCode() {
			int hash = 7;
			hash = 31 * hash + (name == null ? 0 : name.hashCode());
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Format other = (Format) obj;
			return Objects.equals(this.name, other.name);
		}
	}
}
