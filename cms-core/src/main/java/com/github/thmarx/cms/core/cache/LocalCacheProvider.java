package com.github.thmarx.cms.core.cache;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.thmarx.cms.api.cache.CacheManager;
import com.github.thmarx.cms.api.cache.CacheProvider;
import com.github.thmarx.cms.api.cache.ICache;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 *
 * @author t.marx
 */
public class LocalCacheProvider<K extends Serializable, V extends Serializable> implements CacheProvider<K, V> {

	private final ConcurrentMap<String, ICache<K, V>> caches = new ConcurrentHashMap<>();
	
	private Cache<K,V> buildCache (CacheManager.CacheConfig config) {
		var cache = Caffeine.newBuilder();
		
		if (config.maxSize() != null)  {
			cache.maximumSize(config.maxSize());
		}
		if (config.lifeTime() != null) {
			cache.expireAfterAccess(config.lifeTime());
			cache.expireAfterWrite(config.lifeTime());
		}
		
		return cache.build();
	}
	
	@Override
	public ICache<K, V> getCache(String name, CacheManager.CacheConfig config) {
		if (!caches.containsKey(name)) {
			caches.putIfAbsent(name, new LocalCache(
					buildCache(config), 
					(key) -> null));
		}
		return caches.get(name);
	}

	@Override
	public ICache<K, V> getCache(String name, CacheManager.CacheConfig config, Function<K, V> loader) {
		if (!caches.containsKey(name)) {
			caches.putIfAbsent(name, new LocalCache(
					buildCache(config), 
					loader));
		}
		return caches.get(name);
	}
	
}
