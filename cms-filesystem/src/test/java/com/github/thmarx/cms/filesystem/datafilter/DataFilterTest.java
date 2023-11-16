package com.github.thmarx.cms.filesystem.datafilter;

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
