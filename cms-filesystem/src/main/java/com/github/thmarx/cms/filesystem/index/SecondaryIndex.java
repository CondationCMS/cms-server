package com.github.thmarx.cms.filesystem.index;

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

import com.github.thmarx.cms.api.db.ContentNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import lombok.Builder;

/**
 *
 * @author t.marx
 * @param <T>
 */
@Builder
public class SecondaryIndex<T> {
	
	private final Function<ContentNode, T> indexFunction;
	
	private final NavigableMap<T, List<String>> index = new TreeMap<>();
	
	public void clear () {
		index.clear();
	}
	
	public void addAll (final Collection<ContentNode> nodes) {
		nodes.forEach(this::add);
	}
	
	public void add(final ContentNode node) {
		T value = indexFunction.apply(node);
		if (!index.containsKey(value)) {
			index.put(value, new ArrayList<>());
		}
		index.get(value).add(node.uri());
	}
	
	public void remove(final ContentNode node) {
		T value = indexFunction.apply(node);
		if (index.containsKey(value)) {
			index.get(value).remove(node.uri());
		}
	}
	
	public boolean eq (final ContentNode node, final T value) {
		if (index.containsKey(value)) {
			return index.get(value).contains(node.uri());
		}
		return false;
	}
}
