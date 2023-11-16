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
