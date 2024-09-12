package com.condation.cms.core.cache;

/*-
 * #%L
 * cms-core
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


import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import com.condation.cms.api.cache.ICache;
import java.io.Serializable;
import java.util.function.Function;

/**
 *
 * @author t.marx
 * @param <K>
 * @param <V>
 */
@RequiredArgsConstructor
public class LocalCache<K extends Serializable, V extends Serializable> implements ICache<K, V> {
	
	private final Cache<K,V> wrappedCache;
	private final Function<K, V> loader;

	@Override
	public void put(K key, V value) {
		wrappedCache.put(key, value);
	}

	@Override
	public V get(K key) {
		if (!contains(key)) {
			var value = loader.apply(key);
			if (value != null) {
				wrappedCache.put(key, value);
			}
		}
		return wrappedCache.getIfPresent(key);
	}

	@Override
	public boolean contains(K key) {
		return wrappedCache.getIfPresent(key) != null;
	}

	@Override
	public void invalidate() {
		wrappedCache.invalidateAll();
	}

	@Override
	public void invalidate(K key) {
		wrappedCache.invalidate(key);
	}
}
