package com.github.thmarx.cms.filesystem.datafilter;

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
import com.github.thmarx.cms.filesystem.datafilter.dimension.Dimension;
import com.github.thmarx.cms.filesystem.datafilter.dimension.SynchedDimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class DataFilter<T> {

	private Collection<T> items = null;

	private ConcurrentMap<String, Dimension<?, T>> dimensions = new ConcurrentHashMap<>();
	
	public static class Builder<T> {

		public Builder() {

		}

		public DataFilter<T> build() {
			return new DataFilter<>(this);
		}
	}

	/**
	 * @param <BT>
	 * @param clazz the type
	 * @return
	 */
	public static <BT> Builder<BT> builder(Class<BT> clazz) {
		return new Builder<>();
	}

	/**
	 * private constructor
	 *
	 * @param builder
	 */
	private DataFilter(Builder<T> builder) {
		items = Collections.synchronizedCollection(new ArrayList<T>());
	}

	/**
	 * Add a single item to the datafilter.
	 *
	 * @param item
	 */
	public void add(T item) {
		items.add(item);
	}

	/**
	 * Remove a single item from the datafilter
	 *
	 * @param item
	 */
	public void remove(T item) {
		items.remove(item);
	}

	/**
	 * Add a collection of items to the datafilter
	 *
	 * @param items
	 */
	public void addAll(Collection<T> items) {
		this.items.addAll(items);
	}

	/**
	 * Remove a collection if items from the datafilter
	 *
	 * @param items
	 */
	public void removeAll(Collection<T> items) {
		this.items.removeAll(items);
	}

	/**
	 * Clear the items.
	 */
	public void clear() {
		this.items.clear();
	}

	public Dimension<?, T> dimension(final String name) {
		return dimensions.getOrDefault(name, null);
	}
	
	/**
	 * Create a new dimension for the data in the datafilter
	 *
	 * @param <X>
	 * @param vaf
	 * @param clazz
	 * @return
	 */
	public <X> Dimension<X, T> dimension(final String name, Function<T, X> vaf,
			Class<X> clazz) {
		
		if (dimensions.containsKey(name)) {
			return (Dimension<X, T>) dimensions.get(name);
		}
		
		Dimension<X, T> dim = null;

		dim = new SynchedDimension<>(this);

		for (T value : items) {
			X key = vaf.apply(value);
			if (key != null) {
				dim.add(key, value);
			}
		}

		dimensions.put(name, dim);
		
		return dim;
	}

	private static <T> Collection<T> filter(Collection<T> target,
			Function<T, Boolean> predicate) {
		Collection<T> result = new ArrayList<>();
		for (T element : target) {
			if (predicate.apply(element)) {
				result.add(element);
			}
		}
		return result;
	}

	public Collection<T> filter(Function<T, Boolean> predicate) {
		return filter(items, predicate);
	}

	public int size() {
		return items.size();
	}
}
