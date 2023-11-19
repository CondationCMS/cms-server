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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTest {
	public class Person {
		public String name;
		public int age;
		
		public Person () {
			
		}
		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}

	protected Person createPerson() {
		Person p = new Person();
		p.name = TestHelper.randomString();
		p.age = TestHelper.randomInt(50);

		return p;
	}

	protected List<Person> createPersons(int count) {
		List<Person> persons = new ArrayList<Person>();

		for (int i = 0; i < count; i++) {
			persons.add(createPerson());
		}

		return persons;
	}
}
