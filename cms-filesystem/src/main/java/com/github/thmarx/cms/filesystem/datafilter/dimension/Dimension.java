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

import java.util.Collection;
import java.util.Set;

import java.util.function.Consumer;

public interface Dimension<K, V> {
	/**
	 * same as dim.filter(form, to)
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public Collection<V> filterRange(K from, K to);

	public Collection<V> filter(K from, K to);

	/**
	 * same as dim.filter();
	 * 
	 * @return
	 */
	public Collection<V> filterAll();

	public Collection<V> filter();

	/**
	 * same as dim.fitler(key);
	 * 
	 * @param key
	 * @return
	 */
	public Collection<V> filterExact(K key);

	public Collection<V> filter(K key);

	/**
	 *
	 * @param from
	 * @param to
	 * @param returnFunction
	 */
	public void filter(K from, K to,
			Consumer<Collection<V>> returnFunction);

	/**
	 * Add a element to the dimension
	 * 
	 * @param key
	 * @param value
	 */
	public void add(K key, V value);

	/**
	 * Gets the value count
	 * 
	 * @return
	 */
	public int getValueCount();

	/**
	 * Gets the key count
	 * 
	 * @return
	 */
	public int getKeyCount();
	
	public Set<K> keys();

	public boolean isEmpty();
}
