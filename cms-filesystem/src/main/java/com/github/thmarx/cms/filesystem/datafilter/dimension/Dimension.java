package com.github.thmarx.cms.filesystem.datafilter.dimension;

/*-
 * #%L
 * cms-filesystem
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
