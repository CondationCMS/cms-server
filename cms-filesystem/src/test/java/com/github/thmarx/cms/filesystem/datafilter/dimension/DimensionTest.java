package com.github.thmarx.cms.filesystem.dimension;

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

import com.github.thmarx.cms.filesystem.datafilter.DataFilter;
import com.github.thmarx.cms.filesystem.datafilter.dimension.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;



public class DimensionTest {

	@Test
	public void testFilterRange() {
		Collection<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		items.add(5);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();
		df.addAll(items);
		DataFilter.builder(Integer.class).build();

		Dimension<Integer, Integer> dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		Collection<Integer> filtered = dimInt.filterRange(1, 5);
		assertEquals(5, filtered.size());
		assertTrue(contains(filtered, new int[] { 1, 2, 3, 4, 5 }));
		assertTrue(ordered(filtered, new int[] { 1, 2, 3, 4 }));

		filtered = dimInt.filterRange(1, 4);
		assertEquals(4, filtered.size());
		assertTrue(contains(filtered, new int[] { 1, 2, 3, 4 }));
		assertTrue(ordered(filtered, new int[] { 1, 2, 3, 4 }));

		filtered = dimInt.filterRange(2, 4);
		assertEquals(3, filtered.size());
		assertTrue(contains(filtered, new int[] { 2, 3, 4 }));
		assertTrue(ordered(filtered, new int[] { 2, 3, 4 }));

		df.add(3);
		dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		filtered = dimInt.filterRange(2, 4);
		assertEquals(3, filtered.size());
		assertTrue(contains(filtered, new int[] { 2, 3, 4 }));
		assertTrue(ordered(filtered, new int[] { 2, 3, 4 }));
	}

	@Test
	public void testFilterExact() {
		Collection<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(3);
		items.add(3);
		items.add(4);
		items.add(5);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();
		df.addAll(items);

		Dimension<Integer, Integer> dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		Collection<Integer> filtered = dimInt.filterExact(1);
		assertEquals(1, filtered.size());
		assertTrue(contains(filtered, new int[] { 1 }));

		df.add(3);
		dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		filtered = dimInt.filterExact(3);
		assertEquals(2, filtered.size());
		assertTrue(contains(filtered, new int[] { 3, 3 }));
	}

	@Test
	public void testFilterAll() {
		Collection<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		items.add(5);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();
		df.addAll(items);

		Dimension<Integer, Integer> dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		Collection<Integer> filtered = dimInt.filterAll();
		assertEquals(5, filtered.size());
		assertTrue(contains(filtered, new int[] { 1, 2, 3, 4, 5 }));
		assertTrue(ordered(filtered, new int[] { 1, 2, 3, 4, 5 }));
	}

	private boolean contains(Collection<Integer> toTest, int[] cList) {
		for (int i : cList) {
			if (!toTest.contains(i)) {
				return false;
			}
		}

		return true;
	}

	private boolean ordered(Collection<Integer> toTest, int[] cList) {
		int[] testList = toIntArray(toTest);
		for (int i = 0; i < cList.length; i++) {
			if (testList[i] != cList[i]) {
				return false;
			}
		}

		return true;
	}

	private int[] toIntArray(Collection<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list)
			ret[i++] = e.intValue();
		return ret;
	}
}
