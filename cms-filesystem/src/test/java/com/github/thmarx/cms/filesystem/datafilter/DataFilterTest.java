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
import java.util.ArrayList;
import java.util.Collection;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;



public class DataFilterTest {

	@Test
	public void testAddRemove() {
		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();

		df.add(1);

		assertEquals(1, df.size());

		df.remove(1);

		assertEquals(0, df.size());
	}

	@Test
	public void testAddAllRemoveAll() {
		Collection<Integer> items = new ArrayList<>();
		items.add(1);
		items.add(2);
		items.add(3);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();

		df.addAll(items);
		assertEquals(3, df.size());

		df.removeAll(items);
		assertEquals(0, df.size());
	}

	@Test
	public void testDimension() {
		Collection<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(2);
		items.add(3);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();
		df.addAll(items);

		Dimension<Integer, Integer> dimInt = df.dimension("test", (Integer type) -> type, Integer.class);

		assertNotNull(dimInt);

		assertEquals(3, dimInt.getValueCount());
	}
	
	@Test
	public void testDimensionReuse() {
		Collection<Integer> items = new ArrayList<Integer>();
		items.add(1);
		items.add(2);
		items.add(3);

		DataFilter<Integer> df = DataFilter.builder(Integer.class).build();
		df.addAll(items);

		Dimension<Integer, Integer> dimInt1 = df.dimension("test", (Integer type) -> type, Integer.class);
		Dimension<Integer, Integer> dimInt2 = df.dimension("test", (Integer type) -> type, Integer.class);
		Dimension<Integer, Integer> dimInt3 = (Dimension<Integer, Integer>) df.dimension("test");

		Assertions.assertThat(dimInt1).isSameAs(dimInt2);
		Assertions.assertThat(dimInt1).isSameAs(dimInt3);
	}

}
