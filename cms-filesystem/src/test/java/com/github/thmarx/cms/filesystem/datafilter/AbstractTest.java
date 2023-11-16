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
