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
import java.util.List;
import org.junit.jupiter.api.Test;



public class DateFilterPerformanceTest extends AbstractTest {

	@Test
	public void testCreateDateFilter() {

		List<Person> persons = createPersons(1000);

		StopWatch stopWatch = new StopWatch("createDateFilter 1000");

		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		stopWatch.stop();
	}

	@Test
	public void testCreateNameDimesion_1000() {

		List<Person> persons = createPersons(1000);
		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		StopWatch stopWatch = new StopWatch(
				"testCreateNameDimesion_1000");

		Dimension<String, Person> nameDim = personFilter.dimension("test", (Person type) -> type.name, String.class);

		stopWatch.stop();
	}

	@Test
	public void testCreateNameDimesion_10000() {

		List<Person> persons = createPersons(10000);
		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		StopWatch stopWatch = new StopWatch(
				"testCreateNameDimesion_10000");

		Dimension<String, Person> nameDim = personFilter.dimension("test", (Person type) -> type.name, String.class);

		stopWatch.stop();
	}

	@Test
	public void testCreateNameDimesion_100000() {

		List<Person> persons = createPersons(100000);
		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		StopWatch stopWatch = new StopWatch(
				"testCreateNameDimesion_100000");

		Dimension<String, Person> nameDim = personFilter.dimension("test", (Person type) -> type.name, String.class);

		stopWatch.stop();
	}

	@Test
	public void testCreateNameDimesion_500000() {

		List<Person> persons = createPersons(500000);
		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		StopWatch stopWatch = new StopWatch(
				"testCreateNameDimesion_500000");

		Dimension<String, Person> nameDim = personFilter.dimension("test", (Person type) -> type.name, String.class);

		stopWatch.stop();
	}

	@Test
	public void testCreateNameDimesion_1000000() {

		List<Person> persons = createPersons(1000000);
		DataFilter<Person> personFilter = DataFilter.builder(Person.class)
				.build();
		personFilter.addAll(persons);

		StopWatch stopWatch = new StopWatch(
				"testCreateNameDimesion_1000000");

		Dimension<String, Person> nameDim = personFilter.dimension("test", (Person type) -> type.name, String.class);

		stopWatch.stop();
	}

}
