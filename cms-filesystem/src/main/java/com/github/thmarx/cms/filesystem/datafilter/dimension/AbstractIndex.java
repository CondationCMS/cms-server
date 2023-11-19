package com.github.thmarx.cms.filesystem.datafilter.dimension;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import com.github.thmarx.cms.filesystem.datafilter.DataFilter;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractIndex<K, V, M extends NavigableMap<K, List<V>>>
		implements Dimension<K, V> {
	M map;

	protected DataFilter<V> dataFilter;

	public AbstractIndex(M m, DataFilter<V> dataFilter) {
		map = m;
		this.dataFilter = dataFilter;
	}

	protected abstract List<V> createList();

	public synchronized void put(K key, V value) {
		List<V> list = map.get(key);
		if (list == null) {
			list = createList();
			map.put(key, list);
		}
		list.add(value);
	}

	public V get(K key, int index) {
		List<V> list = map.get(key);
		if (list == null) {
			return null;
		}
		if (index >= list.size() || index < 0) {
			return null;
		}
		return list.get(index);
	}

	public synchronized V remove(K key, int index) {
		List<V> list = map.get(key);
		if (list == null) {
			return null;
		}
		if (index >= list.size() || index < 0) {
			return null;
		}
		V v = list.remove(index);
		if (list.isEmpty()) {
			map.remove(key);
		}
		return v;
	}

	@Override
	public int getValueCount() {
		int size = 0;
		for (List<V> list : map.values()) {
			size += list.size();
		}
		return size;
	}

	public int getValueCount(K key) {
		List<V> list = map.get(key);
		if (list == null) {
			return 0;
		}
		return list.size();
	}

	@Override
	public int getKeyCount() {
		return map.size();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public boolean containsKey(K key, int index) {
		List<V> list = map.get(key);
		if (list == null) {
			return false;
		}
		return index < list.size() && index >= 0;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsValue(V targetValue) {
		for (List<V> list : map.values()) {
			for (V value : list) {
				if (targetValue == value)
					return true;
			}
		}
		return false;
	}

	public void clear() {
		map.clear();
	}

	@Override
	public void filter(final K from, final K to,
			final Consumer<Collection<V>> returnFunction) {
		CompletableFuture.runAsync(() -> {
			returnFunction.accept(filter(from, to));
		});
	}

	public void filter(final K key,
			final Consumer<Collection<V>> returnFunction) {
		CompletableFuture.runAsync(() -> {
			returnFunction.accept(filter(key));
		});
	}

	public void filter(final Consumer<Collection<V>> returnFunction) {
		CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
				returnFunction.accept(filterAll());
			}
		});
	}
	
	@Override
	public void add(K key, V value) {
		put(key, value);
	}

	// public int size () {
	// return index.size();
	// }

	/**
	 * same as dim.filter(form, to)
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	@Override
	public Collection<V> filterRange(K from, K to) {
		return filter(from, to);
	}

	@Override
	public Collection<V> filter(K from, K to) {
		Map<K, List<V>> items = map.subMap(from, true, to, true);

		List<V> result = new ArrayList<>();
		for (List<V> list : items.values()) {
			result.addAll(list);
		}

		return result;
	}

	/**
	 * same as dim.filter();
	 * 
	 * @return
	 */
	@Override
	public Collection<V> filterAll() {
		return filter();
	}

	@Override
	public Collection<V> filter() {
		List<V> result = new ArrayList<>();
		for (List<V> list : map.values()) {
			result.addAll(list);
		}

		return result;
	}

	/**
	 * same as dim.fitler(key);
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Collection<V> filterExact(K key) {
		return filter(key);
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	@Override
	public Collection<V> filter(K key) {
		return map.containsKey(key) ?  map.get(key) : Collections.emptyList();
	}
	
	/**
	 *
	 * @return
	 */
	@Override
	public Set<K> keys () {
		return map.keySet();
	}
}
